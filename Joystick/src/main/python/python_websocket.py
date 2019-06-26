import websocket
from websocket import WebSocket

import constants


class WebSocketFeeder:

    def __init__(self, uri):
        self.uri = uri
        self.ws: WebSocket = websocket.create_connection(uri)

    def send(self, status):
        try:
            up = False
            down = False
            left = False
            right = False

            if status & constants.JOYSTICK_LEFT == constants.JOYSTICK_LEFT:
                left = True
            if status & constants.JOYSTICK_RIGHT == constants.JOYSTICK_RIGHT:
                right = True
            if status & constants.JOYSTICK_UP == constants.JOYSTICK_UP:
                up = True
            if status & constants.JOYSTICK_DOWN == constants.JOYSTICK_DOWN:
                down = True

            json = '"left": {}, "right": {}, "up": {}, "down": {} '.format(
                "true" if left else "false",
                "true" if right else "false",
                "true" if up else "false",
                "true" if down else "false")
            json = "{" + json + "}"
            # print("JSON:", json)
            self.ws.send(json)
        except:
            print("Argh!")
            print("Make sure you've started the WebSocket server (here 'node joystick.server.js')")
            print("Also check your proxy settings...")

    def close(self):
        print("Closing WS Feeder")
        self.ws.close()
