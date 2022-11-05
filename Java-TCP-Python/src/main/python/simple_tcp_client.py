#!/usr/bin/env python3

# Good doc at https://realpython.com/python-sockets/#echo-client

import socket
import sys
import traceback
import threading
import signal
from typing import List


# There is a thread to receive
# User input is done in the main thread

machine_name: str = "127.0.0.1"  # localhost
tcp_port: int = 7002
verbose: bool = False

CHUNK_SIZE: int = 1024  # To be used by the server too.

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

print("Usage is:")
print(f"python3 {__file__} [{MACHINE_NAME_PRM_PREFIX}{machine_name}] [{PORT_PRM_PREFIX}{tcp_port}] [{VERBOSE_PREFIX}true|false]")
print(f"\twhere {MACHINE_NAME_PRM_PREFIX} and {PORT_PRM_PREFIX} must match the server's settings.\n")

if len(sys.argv) > 0:  # Script name + X args. > 1 should do the job.
    for arg in sys.argv:
        if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
            machine_name = arg[len(MACHINE_NAME_PRM_PREFIX):]
        if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
            tcp_port = int(arg[len(PORT_PRM_PREFIX):])
        if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
            verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")

if verbose:
    print("-- Received from the command line: --")
    for arg in sys.argv:
        print(f"{arg}")
    print("-------------------------------------")

# Create a TCP/IP socket
sock: socket.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# print(f"sock is a {type(sock)}")

# Connect the socket to the port where the server is listening
server_address: tuple = (machine_name, tcp_port)
# print(f"server_address is a {type(server_address)}")
print('connecting to %s port %s' % server_address)
sock.connect(server_address)
print('...Connected')


def help() -> None:
    print("To exit, type Q, QUIT, or EXIT (lower or upper case)")
    print("To see this message again, type H (lower or upper case)")
    print("To pause the continuous display, type P (lower or upper case)")
    print("To resume a paused display, type R (lower or upper case)")
    input(">> Hit return NOW to move on")


help()

keep_looping: bool = True
paused: bool = False

RED_ON: str = "\033[031m"
RED_OFF: str = "\033[0m"

NMEA_EOS: str = "\r\n"  # aka CR-LF


def keep_receiving(_socket: socket.socket) -> None:
    global keep_looping
    while keep_looping:
        # Wait for the response
        data: str = _socket.recv(CHUNK_SIZE).decode("utf-8")
        #
        if not paused:
            if verbose:
                print(f"\treceived '{data.strip()}' ({type(data)})")
            if len(data.strip()) > 0:
                # "\033[031m" + "Hello" + "\033[0m"
                # Split sentences
                sentences: List[str] = data.strip().split(NMEA_EOS)  #
                if verbose:
                    print(f"{len(sentences)} sentence(s)")
                for sentence in sentences:
                    print(f"Data from Server: {RED_ON}{sentence.strip()}{RED_OFF}")
                # print(f"Data from Server: {RED_ON}{data.strip()}{RED_OFF}")  # As received
            else:
                print("Received dummy ping...")
                # Server might be down, exiting.
                keep_looping = False
                print("Bye")


def interrupt(signal, frame):
    print("\nCtrl+C intercepted!")
    global keep_looping
    keep_looping = False

signal.signal(signal.SIGINT, interrupt)  # callback defined above.

# Here we start the listener thread
try:
    listener: threading.Thread = threading.Thread(name="ClientListener", target=keep_receiving, args=(sock,))
    listener.daemon = True  # Dies on exit
    listener.start()
except Exception as ex:
    print("Exception!")
    traceback.print_exc(file=sys.stdout)

# Interactive (client) loop (min thread).
# Nothing is sent to the TCP server.
while keep_looping:
    user_input: str = input()  # Blind input
    if user_input.upper() == 'Q' or user_input.upper() == 'QUIT' or user_input.upper() == 'EXIT':
        keep_looping = False
    else:
        try:
            if len(user_input) > 0:   # User's message must not be empty. I've decided.
                if user_input.upper() == 'H':    # Help
                    help()
                elif user_input.upper() == 'P':  # Pause
                    paused = True
                elif user_input.upper() == 'R':  # Resume
                    paused = False
                else:  # Send to server, with a NL at the end.
                    sock.sendall((user_input + '\n').encode())
            else:
                print("No empty message please.")
        except Exception as ex:
            print("Exception: {}".format(ex))
            traceback.print_exc(file=sys.stdout)
        # finally:
        #     print('closing socket')
        #     sock.close()


print('closing socket')
sock.close()
