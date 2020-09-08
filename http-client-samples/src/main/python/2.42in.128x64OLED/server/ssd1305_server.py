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

import digitalio
import busio
import board
from PIL import Image, ImageDraw, ImageFont
import adafruit_ssd1305

# Define the Reset Pin
oled_reset = digitalio.DigitalInOut(board.D4)

sample_data = {  # Used for VIEW, and non-implemented operations. Fallback.
    "1": "First",
    "2": "Second",
    "3": "Third",
    "4": "Fourth"
}
server_port = 8080
REST_DEBUG = False

# Change these
# to the right size for your display!
WIDTH = 128
HEIGHT = 64  # Change to 32 if needed
BORDER = 0  # 2

spi = board.SPI()
oled_cs = digitalio.DigitalInOut(board.D5)
oled_dc = digitalio.DigitalInOut(board.D6)
oled = adafruit_ssd1305.SSD1305_SPI(WIDTH, HEIGHT, spi, oled_dc, oled_reset, oled_cs)

# Use for I2C.
# i2c = board.I2C()
# oled = adafruit_ssd1305.SSD1305_I2C(WIDTH, HEIGHT, i2c, addr=0x3c, reset=oled_reset)

font_size = 10
font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", font_size)


def cls():
    # Clear display.
    oled.fill(0)
    oled.show()


def new_display():
    # Create blank image for drawing.
    # Make sure to create image with mode '1' for 1-bit color.
    image = Image.new("1", (oled.width, oled.height))

    # Get drawing object to draw on image.
    draw = ImageDraw.Draw(image)
    return image, draw


def write_on_ssd1305(draw, text, x, y, font):
    draw.text(
    (x, y),
    text,
    font=font,
    fill=255)


def display(image):
    # Display image
    oled.image(image)
    oled.show()


PATH_PREFIX = "/ssd1305"


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
    def log_message(self, format, *args):
        if REST_DEBUG:
            print(format % args)
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

        if path == PATH_PREFIX + "/oplist":
            response = {
                "oplist": [{
                        "path": PATH_PREFIX + "/oplist",
                        "verb": "GET",
                        "description": "Get the available operation list."
                    }, {
                        "path": PATH_PREFIX + "/display",
                        "verb": "POST",
                        "description": "Display some text on screen."
                    }, {
                        "path": PATH_PREFIX + "/clean",
                        "verb": "POST",
                        "description": "Clear the screen."
                    }]
            }
            response_content = json.dumps(response).encode()
            self.send_response(200)
            # defining the response headers
            # self.send_header('Content-Type', 'application/json')
            # content_len = len(response_content)
            # self.send_header('Content-Length', content_len)
            # self.end_headers()
            self.wfile.write(response_content)
        else:
            if REST_DEBUG:
                print("GET on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(400)
            self.send_header('Content-Type', 'plain/text')
            content_len = len(error)
            self.send_header('Content-Length', content_len)
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # VIEW method definition. Uncommon...
    def do_VIEW(self):
        # dict var. for pretty print
        display = {}
        temp = self._set_headers()
        # check if the key is present in the sample_data dictionary
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

    # POST method definition
    def do_POST(self):
        if REST_DEBUG:
            print("POST request, {}".format(self.path))
        if self.path == PATH_PREFIX + "/display":
            # Get text to display from body (text/plain)
            content_len = int(self.headers.get('Content-Length'))
            post_body = self.rfile.read(content_len).decode('utf-8')
            print("Content: {}".format(post_body))
            try:
                text = post_body
                cls()
                (image, draw) = new_display()
                write_on_ssd1305(draw, text, 2, font_size, font)
                display(image)
                # Response
                self.send_response(201)
                self.send_header('Content-Type', 'application/json')
                response = {"status": "OK"}
                response_content = json.dumps(response).encode()
                content_len = len(response_content)
                self.send_header('Content-Length', content_len)
                self.end_headers()
                self.wfile.write(response_content)
            except:
                self.send_response(500)
                response = {"status": "Barf"}
                self.wfile.write(json.dumps(response).encode())
        elif self.path == PATH_PREFIX + "/clean":
            try:
                cls()
                (image, draw) = new_display()
                display(image)
                # Response
                self.send_response(201)
                self.send_header('Content-Type', 'application/json')
                response = {"status": "OK"}
                response_content = json.dumps(response).encode()
                content_len = len(response_content)
                self.send_header('Content-Length', content_len)
                self.end_headers()
                self.wfile.write(response_content)
            except:
                self.send_response(500)
                response = {"status": "Barf"}
                self.wfile.write(json.dumps(response).encode())
        else:
            if REST_DEBUG:
                print("POST on {} not managed".format(self.path))
            error = "NOT FOUND!"
            self.send_response(404)
            self.send_header('Content-Type', 'plain/text')
            content_len = len(error)
            self.send_header('Content-Length', content_len)
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # self.wfile.write(json.dumps(data[str(index)]).encode())

    # PUT method Definition
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
            self.send_header('Content-Type', 'plain/text')
            content_len = len(error)
            self.send_header('Content-Length', content_len)
            self.end_headers()
            self.wfile.write(bytes(error, 'utf-8'))

    # DELETE method definition
    def do_DELETE(self):
        if REST_DEBUG:
            print("DELETE on {} not managed".format(self.path))
        error = "NOT FOUND!"
        self.send_response(400)
        self.send_header('Content-Type', 'plain/text')
        content_len = len(error)
        self.send_header('Content-Length', content_len)
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
print("Try curl -X GET http://{}:{}{}/oplist".format(machine_name, port_number, PATH_PREFIX))
print("or  curl -v -X VIEW http://{}:{}{} -H \"Content-Length: 1\" -d \"1\"".format(machine_name, port_number, PATH_PREFIX))
#
try:
    server.serve_forever()
except KeyboardInterrupt:
    print("\n\t\tUser interrupted (server.serve), exiting.")

cls()
print("Done")
