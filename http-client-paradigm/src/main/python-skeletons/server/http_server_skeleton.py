#!/usr/bin/env python3
#
# May requires (if below python 3.7):
# -----------------------------------
# pip3 install http (already in python3.7, no need to install it)
#
# Provides REST access to the cache, try GET http://localhost:8080/sample/cache
# Implement your own features in the do_stuff method and the resources in the ServiceHandler class...
#
# The main is at the bottom.
#
import json
import sys
import threading
import traceback
import time
from http.server import HTTPServer, BaseHTTPRequestHandler
from time import sleep

sample_data = {  # Used for VIEW, and non-implemented operations. Fallback.
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

keep_looping = True


def do_stuff():
    print("Let's go. Hit Ctrl+C to stop")
    global keep_looping
    while keep_looping:
        try:
            try:
                core.update_cache('position', 'data goes here')
                core.update_cache('timestamp', time.time())
            except AttributeError as ae:
                print("AttributeError : {}".format(ae))
        except KeyboardInterrupt:
            print("\n\t\tUser interrupted, exiting.")
            keep_looping = False
            x.join()
            break
        except:
            # print("\t\tOoops! {}: {}".format(type(ex), ex))
            traceback.print_exc(file=sys.stdout)
        sleep(1.0)  # one sec between loops
    print("Bye.")


# Start doing the core job (read GPS, etc)
try:
    print("Starting!")
    x = threading.Thread(target=do_stuff)
    x.start()
except OSError as ose:
    print(ose)
    sys.exit(1)  # Bam!

PATH_PREFIX = "/sample"


# Defining a HTTP request Handler class
class ServiceHandler(BaseHTTPRequestHandler):
    # sets basic headers for the server
    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        # reads the length of the Headers
        length = int(self.headers['Content-Length'])
        # reads the contents of the request
        content = self.rfile.read(length)
        temp = str(content).strip('b\'')
        self.end_headers()
        return temp

    # To silence the HTTP logger
    def log_message(self, fmt, *args):
        return

    # GET Method Definition
    def do_GET(self):
        if REST_DEBUG:
            print("GET methods")
        # defining all the headers
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
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
                response_content = json.dumps(gps_cache).encode()
                self.send_response(200)
                # defining the response headers
                self.send_header('Content-Type', 'application/json')
                content_len = len(response_content)
                self.send_header('Content-Length', str(content_len))
                self.end_headers()
                self.wfile.write(response_content)
            except Exception as exception:
                error = {"message": "{}".format(exception)}
                self.wfile.write(json.dumps(error).encode())
                self.send_response(500)
        else:
            if REST_DEBUG:
                print("GET on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(400)
            self.send_header('Content-Type', 'plain/text')
            content_len = len(error)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # VIEW method definition.
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
            error = "{} Not found in sample_data\n".format(temp)
            self.send_response(404)
            self.send_header('Content-Type', 'plain/text')
            content_len = len(error)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # POST method definition
    def do_POST(self):
        if REST_DEBUG:
            print("POST request, {}".format(self.path))
        if self.path.startswith("/whatever"):
            content_len = int(self.headers.get('Content-Length'))
            post_body = self.rfile.read(content_len).decode('utf-8')
            print("Content: {}".format(post_body))

            response = {"status": "OK"}
            response_content = json.dumps(response).encode()
            self.send_response(201)
            # defining the response headers
            self.send_header('Content-Type', 'application/json')
            content_len = len(response_content)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(response_content)
        else:
            if REST_DEBUG:
                print("POST on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(404)
            self.send_header('Content-Type', 'plain/text')
            content_len = len(error)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # self.wfile.write(json.dumps(data[str(index)]).encode())

    # PUT method Definition
    def do_PUT(self):
        if REST_DEBUG:
            print("PUT request, {}".format(self.path))
        if self.path.startswith("/whatever"):
            response = {"status": "OK"}
            response_content = json.dumps(response).encode()
            self.send_response(201)
            # defining the response headers
            self.send_header('Content-Type', 'application/json')
            content_len = len(response_content)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(response_content)
        else:
            if REST_DEBUG:
                print("PUT on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(404)
            self.send_header('Content-Type', 'plain/text')
            content_len = len(error)
            self.send_header('Content-Length', str(content_len))
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # DELETE method definition
    def do_DELETE(self):
        if REST_DEBUG:
            print("DELETE on {} not managed".format(self.path))
        error = "NOT FOUND!"
        self.send_response(404)
        self.send_header('Content-Type', 'plain/text')
        content_len = len(error)
        self.send_header('Content-Length', str(content_len))
        self.end_headers()
        self.wfile.write(bytes(error, 'utf-8'))


machine_name = "127.0.0.1"
MACHINE_NAME_PRM_PREFIX = "--machine-name:"
PORT_PRM_PREFIX = "--port:"
VERBOSE_PREFIX = "--verbose:"

if len(sys.argv) > 0:  # Script name + X args
    for arg in sys.argv:
        if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
            machine_name = arg[len(MACHINE_NAME_PRM_PREFIX):]
        if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
            server_port = int(arg[len(PORT_PRM_PREFIX):])
        if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
            REST_DEBUG = (arg[len(VERBOSE_PREFIX):].lower() == "true")

# Server Initialization
port_number = server_port
print("Starting server on port {}".format(port_number))
server = HTTPServer((machine_name, port_number), ServiceHandler)
#
print("Try curl -v -X GET http://{}:{}/{}/cache".format(machine_name, port_number, PATH_PREFIX))
print("or  curl -v -X VIEW http://{}:{}{} -H \"Content-Length: 1\" -d \"1\"".format(machine_name, port_number,
                                                                                    PATH_PREFIX))
#
try:
    server.serve_forever()
except KeyboardInterrupt:
    print("\n\t\tUser interrupted (server.serve), exiting.")
    keep_looping = False
    x.join()
