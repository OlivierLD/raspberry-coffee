#!/usr/bin/env python3
#
# Requires:
# ---------
# pip3 install http (already in python3.7, no need to install it)
#
# Provides REST access to the cache, try GET http://localhost:8080/lis3mdl/cache
#
import json
import sys
import threading
import traceback
import time
from http.server import HTTPServer, BaseHTTPRequestHandler
from time import sleep

import board
import busio
import adafruit_lis3mdl

sample_data = {  # Used for non-implemented operations. Fallback.
    "1": "First",
    "2": "Second",
    "3": "Third",
    "4": "Fourth"
}
server_port = 8080
REST_DEBUG = False


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

i2c = busio.I2C(board.SCL, board.SDA)
sensor = adafruit_lis3mdl.LIS3MDL(i2c)


def read_lis3mdl():
    print("Let's go. Hit Ctrl+C to stop")
    while True:
        try:
            try:
                mag_x, mag_y, mag_z = sensor.magnetic
                core.update_cache('x', mag_x)
                core.update_cache('y', mag_y)
                core.update_cache('z', mag_z)
            except AttributeError as ae:
                print("AttributeError : {}".format(ae))
        except KeyboardInterrupt:
            print("\n\t\tUser interrupted, exiting.")
            break
        except:
            # print("\t\tOoops! {}: {}".format(type(ex), ex))
            traceback.print_exc(file=sys.stdout)
        sleep(1.0)  # one sec between loops
    print("Bye.")


# Start doing the core job (read GPS, etc)
try:
    print("Starting!")
    x = threading.Thread(target=read_lis3mdl)
    x.start()
except OSError as ose:
    print(ose)
    sys.exit(1)  # Bam!

PATH_PREFIX = "/lis3mdl"


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

        if path == PATH_PREFIX + "/cache":  # GET /sample/cache
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


machine_name = "127.0.0.1"
MACHINE_NAME_PRM_PREFIX = "--machine-name:"
PORT_PRM_PREFIX = "--port:"


if len(sys.argv) > 0:  # Script name + X args
    for arg in sys.argv:
        if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
            machine_name = arg[len(MACHINE_NAME_PRM_PREFIX):]
        if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
            server_port = int(arg[len(PORT_PRM_PREFIX):])

# Server Initialization
port_number = server_port
print("Starting server on port {}".format(port_number))
server = HTTPServer((machine_name, port_number), ServiceHandler)
#
print("Try curl -X GET http://{}:{}/{}/cache".format(machine_name, port_number, PATH_PREFIX))
#
server.serve_forever()
