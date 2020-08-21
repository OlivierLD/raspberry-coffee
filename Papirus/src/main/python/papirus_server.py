#!/usr/bin/env python3
#
# Requires:
# ---------
# pip3 install http (already in python3.7, no need to install it)
#
# Provides REST access to the cache, try GET http://localhost:8080/sample/cache
# Implement your own features in the do_stuff method and the resources in the ServiceHandler class...
#
# Run like this:
#   python3 papirus_server.py --machine-name:$(hostname -I)
#
import json
import sys
import threading
import traceback
import time
from http.server import HTTPServer, BaseHTTPRequestHandler
from time import sleep
from papirus import PapirusText   # See the README.md
from papirus import PapirusImage

rot = 00
papirus_display = PapirusText(rotation=rot)
papirus_image   = PapirusImage(rotation=rot)

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

    def display_papirus(self, text, font_size=24):
        papirus_display.write(text, size=font_size)

    def clear_papirus(self):
        # papirus_display.clear()  # clear works only on PapirusTextPos
        papirus_display.write("")

    def image_papirus(self, img_location="./pelican.bw.png"):
        papirus_image.write(img_location)

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

def do_stuff():
    print("Let's go. Hit Ctrl+C to stop")
    while True:
        try:
            try:
                core.update_cache('position', 'data goes here')
                core.update_cache('timestamp', time.time())
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


# Start doing the core job (reading button clicks, etc)
if False:
    try:
        print("Starting!")
        x = threading.Thread(target=do_stuff)
        x.start()
    except OSError as ose:
        print(ose)
        sys.exit(1)  # Bam!


PATH_PREFIX = "/papirus"


# Defining a HTTP request Handler class
class ServiceHandler(BaseHTTPRequestHandler):
    # sets basic headers for the server
    def _set_headers(self, status=200):
        self.send_response(status)
        self.send_header('Content-type', 'application/json')
        # reads the length of the Headers
        length = int(self.headers['Content-Length'])
        # reads the contents of the request
        content = self.rfile.read(length)
        temp = str(content).strip('b\'')
        self.end_headers()
        return temp

    # To silence the HTTP logger
    def log_message(self, format, *args):
        return

    # GET Method Definition
    def do_GET(self):
        if REST_DEBUG:
            print("GET methods")
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

        if path == PATH_PREFIX + "/cache":
            if REST_DEBUG:
                print("Cache request")
            # defining all the headers
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            try:
                gps_cache = core.get_cache()
                self.wfile.write(json.dumps(gps_cache).encode())
            except Exception as exception:
                error = {"message": "{}".format(exception)}
                self.wfile.write(json.dumps(error).encode())
                self.send_response(500)
        if path == PATH_PREFIX + "/oplist":  # Just an example ;)
            response = {
                "oplist": [
                    {
                        "path": PATH_PREFIX + "/oplist",
                        "verb": "GET",
                        "description": "Get the available operation list."
                    },
                    {
                        "path": PATH_PREFIX + "/cache",
                        "verb": "GET",
                        "description": "Dummy example, meaningless here."
                    },
                    {
                        "path": PATH_PREFIX + "/display",
                        "verb": "POST",
                        "parameters" : [
                            {
                                "type": "query",
                                "name": "font_size",
                                "mandatory": False
                            },
                            {
                                "type": "body",
                                "content-type": "plain/text",
                                "mandatory": True
                            }
                        ],
                        "description": "Display text on the screen."
                    },
                    {
                        "path": PATH_PREFIX + "/clear",
                        "verb": "POST",
                        "description": "Clear the screen."
                    },
                    {
                        "path": PATH_PREFIX + "/image",
                        "verb": "POST",
                        "parameters" : [
                            {
                                "type": "query",
                                "name": "image_path",
                                "mandatory": False
                            }
                        ],
                        "description": "Display an image on the screen."
                    }
                ]
            }
            response_content = json.dumps(response).encode()
            self.send_response(200)
            # defining the response headers
            self.send_header('Content-type', 'application/json')
            content_len = len(response_content)
            self.send_header('Content-Length', content_len)
            self.end_headers()
            self.wfile.write(response_content)
        else:
            if REST_DEBUG:
                print("GET on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(400)
            self.send_header('Content-type', 'plain/text')
            content_len = len(error)
            self.send_header('Content-Length', content_len)
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
            self.send_header('Content-Length', content_len)
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # POST method definition - Only one for now
    def do_POST(self):
        if REST_DEBUG:
            print("POST request, {}".format(self.path))

        content_type = self.headers.get('Content-Type')
        content_len = int(self.headers.get('Content-Length'))
        post_body = self.rfile.read(content_len).decode("utf-8")  # byte[] to string

        full_path = self.path
        split = full_path.split('?')
        path = split[0]
        qs = None
        if len(split) > 1:
            qs = split[1]
        # The QS parameters into a map
        prm_map = {}
        if qs is not None:
            qs_prms = qs.split('&')
            for qs_prm in qs_prms:
                nv_pair = qs_prm.split('=')
                if len(nv_pair) == 2:
                    prm_map[nv_pair[0]] = nv_pair[1]
                else:
                    print("oops, no equal sign in {}".format(qs_prm))

        if REST_DEBUG:
            print("Type: {}, len: {}".format(content_type, content_len))
            print("Content: {}".format(post_body))

        if path == PATH_PREFIX + "/display":
            # Get data to display here, in the body, as plain/text
            # TODO Check on Content-type "plain/text" ?
            font_size = int(prm_map.get("font_size"))
            data_to_display = post_body   # "Akeu Coucou"
            if font_size is not None:
                core.display_papirus(data_to_display, font_size=font_size)
            else:
                core.display_papirus(data_to_display)
            self.send_response(201)
            response = {"status": "OK"}
            response_content = json.dumps(response).encode()
            # defining the response headers
            self.send_header('Content-type', 'application/json')
            content_len = len(response_content)
            self.send_header('Content-Length', content_len)
            self.end_headers()
            self.wfile.write(response_content)
        elif path == PATH_PREFIX + "/clear":
            core.clear_papirus()
            self.send_response(201)
            response = {"status": "OK"}
            response_content = json.dumps(response).encode()
            # defining the response headers
            self.send_header('Content-type', 'application/json')
            content_len = len(response_content)
            self.send_header('Content-Length', content_len)
            self.end_headers()
            self.wfile.write(response_content)
        elif path == PATH_PREFIX + "/image":
            img_loc = prm_map.get("image_path")
            if img_loc is None:
                core.image_papirus()
            else:
                core.image_papirus(img_location=img_loc)
            self.send_response(201)
            response = {"status": "OK"}
            response_content = json.dumps(response).encode()
            # defining the response headers
            self.send_header('Content-type', 'application/json')
            content_len = len(response_content)
            self.send_header('Content-Length', content_len)
            self.end_headers()
            self.wfile.write(response_content)
        else:
            if REST_DEBUG:
                print("POST on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(404)
            self.send_header('Content-type', 'plain/text')
            content_len = len(error)
            self.send_header('Content-Length', content_len)
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # self.wfile.write(json.dumps(data[str(index)]).encode())

    # PUT method Definition - Not used
    def do_PUT(self):
        if REST_DEBUG:
            print("PUT request, {}".format(self.path))
        if self.path.startswith("/whatever/"):
            self.send_response(201)
            response = {"status": "OK"}
            self.wfile.write(json.dumps(response).encode())
        else:
            if REST_DEBUG:
                print("PUT on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(404)
            self.send_header('Content-type', 'plain/text')
            content_len = len(error)
            self.send_header('Content-Length', content_len)
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # DELETE method definition - Not used
    def do_DELETE(self):
            if REST_DEBUG:
                print("DELETE on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(404)
            self.send_header('Content-type', 'plain/text')
            content_len = len(error)
            self.send_header('Content-Length', content_len)
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))


machine_name = "127.0.0.1"  # Should be overridden with actual IP address...
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
            REST_DEBUG = (arg[len(VERBOSE_PREFIX):] == "true")

# Server Initialization
port_number = server_port
print("Starting server on port {}".format(port_number))
server = HTTPServer((machine_name, port_number), ServiceHandler)
#
print("Try curl -X GET http://{}:{}/{}/oplist".format(machine_name, port_number, PATH_PREFIX))
#
server.serve_forever()  # There we go!
