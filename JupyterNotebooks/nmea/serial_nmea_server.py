#!/usr/bin/env python3
#
# Requires:
# pip3 install http
# pip3 install pyserial
#
# Reads the Serial Port
# Parses the NMEA Data
# Pushes the parsed data in a cache
# Provides REST access to the cache, try http://localhost:8080/gps/cache
#
import json
import sys
import threading
import traceback
from http.server import HTTPServer, BaseHTTPRequestHandler

import serial

import nmea_parser as NMEAParser

sample_data = {  # Used for non-implemented operations. Fallback.
    "1": "First",
    "2": "Second",
    "3": "Third",
    "4": "Fourth"
}
server_port = 8080
REST_DEBUG = False
SERIAL_DEBUG = False
GPS_DEBUG = False


# On mac, USB GPS on port /dev/tty.usbmodem14101,
# Raspberry Pi, use /dev/ttyUSB0 or so.
port_name = "/dev/tty.usbmodem14201"
# port_name = "/dev/ttyS80"
baud_rate = 4800


class CoreFeatures:
    """
    Implements the methods used in the REST operations below
    """

    cache = {}

    def update_cache(self, key, value):
        try:
            if REST_DEBUG:
                print("Putting {} as {} in {}".format(value, key, self.cache))
            self.cache[key] = value
            if REST_DEBUG:
                print("Cache is now {}".format(self.cache))
        except KeyError as ke:
            if REST_DEBUG:
                print("KeyError {}".format(ke))
        except Exception as ex:
            print("in update_cache: {}: {}, key {}, data {}".format(type(ex), ex, key, value))

    def get_cache(self):
        return self.cache


core = CoreFeatures()


def read_nmea_sentence(serial_port):
    """
    Reads the serial port until a '\n' is met.
    :param serial_port: the port to read, as returned by serial.Serial
    :return: the full NMEA String, with its EOS '\r\n'
    """
    rv = []
    while True:
        try:
            ch = serial_port.read()
        except KeyboardInterrupt as ki:
            raise ki
        if SERIAL_DEBUG:
            print("Read {} from Serial Port".format(ch))
        rv.append(ch)
        if ch == b'\n':
            # string = [x.decode('utf-8') for x in rv]
            string = "".join(map(bytes.decode, rv))
            if SERIAL_DEBUG:
                print("Returning {}".format(string))
            return string


def read_gps():
    port = serial.Serial(port_name, baudrate=baud_rate, timeout=3.0)
    print("Let's go. Hit Ctrl+C to stop")
    while True:
        try:
            rcv = read_nmea_sentence(port)
            # print("\tReceived:" + repr(rcv))  # repr: displays also non printable characters between quotes.
            nmea_obj = NMEAParser.parse_nmea_sentence(rcv)
            try:
                if nmea_obj["type"] == 'rmc':
                    if 'valid' in nmea_obj['parsed'] and nmea_obj['parsed']['valid'] == 'true':
                        if 'position' in nmea_obj['parsed']:
                            if GPS_DEBUG:
                                print("RMC => {}".format(nmea_obj))
                                print("This is RMC: {} / {}".format(
                                    NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['latitude'], NMEAParser.NS),
                                    NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['longitude'], NMEAParser.EW)))
                            core.update_cache('position', nmea_obj['parsed']['position'])
                        if 'sog' in nmea_obj['parsed']:
                            core.update_cache('sog', nmea_obj['parsed']['sog'])
                        if 'cog' in nmea_obj['parsed']:
                            core.update_cache('cog', nmea_obj['parsed']['cog'])
                        if 'utc-date-itemized' in nmea_obj['parsed']:
                            core.update_cache('utc-date', nmea_obj['parsed']['utc-date-itemized'])
                    else:
                        if GPS_DEBUG:
                            print("No position yet")
                elif nmea_obj["type"] == 'gll':
                    if 'valid' in nmea_obj['parsed'] and nmea_obj['parsed']['valid'] == 'true':
                        if 'position' in nmea_obj['parsed']:
                            if GPS_DEBUG:
                                print("GLL => {}".format(nmea_obj))
                                print("This is GLL: {} / {}".format(
                                    NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['latitude'], NMEAParser.NS),
                                    NMEAParser.dec_to_sex(nmea_obj['parsed']['position']['longitude'], NMEAParser.EW)))
                            core.update_cache('position', nmea_obj['parsed']['position'])
                        if 'gll-time-itemized' in nmea_obj['parsed']:
                            core.update_cache('gll-time', nmea_obj['parsed']['gll-time-itemized'])
                    else:
                        if GPS_DEBUG:
                            print("No position yet")
                else:
                    if GPS_DEBUG:
                        print("{} => {}".format(nmea_obj["type"], nmea_obj))
            except AttributeError as ae:
                print("AttributeError for {}: {}".format(nmea_obj, ae))
        except NMEAParser.NoParserException as npe:
            # absorb
            if GPS_DEBUG:
                print("- No parser, {}".format(npe))
        except KeyboardInterrupt:
            print("\n\t\tUser interrupted, exiting.")
            port.close()
            break
        except:
            # print("\t\tOoops! {}: {}".format(type(ex), ex))
            traceback.print_exc(file=sys.stdout)

    print("Bye.")


# Start polling Serial port here
try:
    print("Starting!")
    x = threading.Thread(target=read_gps)
    x.start()
except OSError as ose:
    print(ose)
    sys.exit(1)  # Bam!

PATH_PREFIX = "/gps"


# Defining a HTTP request Handler class
class ServiceHandler(BaseHTTPRequestHandler):
    # sets basic headers for the server
    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        # reads the length of the Headers
        length = int(self.headers['Content-Length'])
        # reads the contents of the request
        content = self.rfile.read(length)
        temp = str(content).strip('b\'')
        self.end_headers()
        return temp

    # GET Method Definition
    def do_GET(self):
        print("GET methods")
        # defining all the headers
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        #
        full_path = self.path
        split = full_path.split('?')
        path = split[0]
        qs = None
        if len(split) > 1:
            qs = split[1]
        # The parameters into a map
        prm_map = {}
        if qs is not None:
            qs_prms = qs.split('&')
            for qs_prm in qs_prms:
                nv_pair = qs_prm.split('=')
                if len(nv_pair) == 2:
                    prm_map[nv_pair[0]] = nv_pair[1]
                else:
                    print("oops, no equal sign in {}".format(qs_prm))

        if path == PATH_PREFIX + "/cache":  # GET /gps/cache
            print("Cache request")
            try:
                gps_cache = core.get_cache()
                self.wfile.write(json.dumps(gps_cache).encode())
            except Exception as exception:
                error = {"message": "{}".format(exception)}
                self.wfile.write(json.dumps(error).encode())
                self.send_response(500)
        else:
            # prints all the keys and values of the json file
            self.wfile.write(json.dumps("Not managed (yet)").encode())
            self.send_response(400)

    # VIEW method definition. WTF ? (What the French)
    def do_VIEW(self):
        # dict var. for pretty print
        display = {}
        temp = self._set_headers()
        # check if the key is present in the dictionary
        if temp in sample_data:
            display[temp] = sample_data[temp]
            # print the keys required from the json file
            self.wfile.write(json.dumps(display).encode())
        else:
            error = "NOT FOUND!"
            self.wfile.write(bytes(error, 'utf-8'))
            self.send_response(404)

    # POST method definition
    def do_POST(self):
        print("POST request, {}".format(self.path))
        if self.path.startswith("/whatever/"):
            self.send_response(201)
            response = {"status": "OK"}
            self.wfile.write(json.dumps(response).encode())
        else:
            error = "NOT FOUND!"
            self.wfile.write(bytes(error, 'utf-8'))
            self.send_response(404)

    # self.wfile.write(json.dumps(data[str(index)]).encode())

    # PUT method Definition
    def do_PUT(self):
        print("PUT request, {}".format(self.path))
        if self.path.startswith("/whatever/"):
            self.send_response(201)
            response = {"status": "OK"}
            self.wfile.write(json.dumps(response).encode())
        else:
            error = "NOT FOUND!"
            self.wfile.write(bytes(error, 'utf-8'))
            self.send_response(404)

    # DELETE method definition
    def do_DELETE(self):
        error = "NOT FOUND!"
        self.wfile.write(bytes(error, 'utf-8'))
        self.send_response(404)


# Server Initialization
port_number = server_port
print("Starting server on port {}".format(port_number))
server = HTTPServer(('127.0.0.1', port_number), ServiceHandler)
#
print("Try curl -X GET http://localhost:8080/gps/cache")
#
server.serve_forever()
