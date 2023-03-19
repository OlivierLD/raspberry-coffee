#!/usr/bin/python
#
# From https://github.com/itemir/rpi_boat_utils/tree/master/gpsd2nmea
#
# Copyright (c) 2017 Ilker Temir
#
# MIT License
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#
# Use python2
#

import argparse
import os
import socket
import sys
import threading

clients = set()
addresses = {}
clients_lock = threading.Lock()

def listener(client, address):
    if cli_options.verbose:
        print "New connection %s." % str(address)
    addresses[client.fileno()] = address
    with clients_lock:
        clients.add(client)
    try:
        while True:
            pass
    finally:
        with clients_lock:
            clients.remove(client)
            client.close()

def read_ais_messages(gpsd):
    while True:
        try:
            ais_message = gpsd.recv(2048)
            print 'AIS Message: %s' % ais_message
        except (IOError, socket.error):
            print 'Error reading from gpsd'
            sys.exit(1)
        if cli_options.verbose:
            log = ais_message.strip()
            print 'Received NMEA sentence: %s' % log
        clients_to_remove = set()
        clients_lock.acquire()
        for client in clients:
            try:
                client.sendall(ais_message)
            except (IOError, socket.error):
                clients_to_remove.add(client)
        for client in clients_to_remove:
            if cli_options.verbose:
                print "Connection dropped %s." % str(addresses[client.fileno()])
            clients.remove(client)
        clients_lock.release()

parser = argparse.ArgumentParser()
parser.add_argument('--gpsd-server',
                    dest='gpsd_server',
                    default='127.0.0.1',
                    help='gpsd server address (default: 127.0.0.1)')
parser.add_argument('--gpsd-port',
                    dest='gpsd_port',
                    type=int,
                    default=2947,
                    help='gpsd port number (default: 2947)')
parser.add_argument('--port',
                    dest='port',
                    type=int,
                    default=2948,
                    help='Listen on TCP port number (default: 2948)')
parser.add_argument('-v',
                    '--verbose',
                    dest='verbose',
                    action='store_true',
                    help='Enable debug messages')
cli_options = parser.parse_args()

try:
    gpsd = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    gpsd.connect((cli_options.gpsd_server, cli_options.gpsd_port))
    gpsd.send('?WATCH={"enable":true,"json":false,"nmea":true,"raw":0,"scaled":false,"timing":false,"split24":false,"pps":false}')
except (IOError, socket.error):
    print 'Error opening connection to %s:%s' % (cli_options.gpsd_server, cli_options.gpsd_port)
    sys.exit(1)

s = socket.socket()
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind(('0.0.0.0',cli_options.port))
s.listen(3)

ais_thread = threading.Thread(target=read_ais_messages, args=(gpsd,))
ais_thread.start()

th = []
while True:
    client, address = s.accept()
    th.append(threading.Thread(target=listener, args = (client,address)).start())

s.close()
