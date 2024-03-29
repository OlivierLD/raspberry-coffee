import websocket
import sys
import constants
import json

__author__ = 'Olivier LeDiouris'
__version__ = '0.0.5'

class WebSocketFeeder:
    """
    Feed a WebSocket server.
    Provide ws uri as parameter of the constructor (like 'ws://localhost:9876')
    """
    def __init__(self, uri):
        self.uri = uri
        self.ws = None
        try:
            self.ws = websocket.create_connection(uri)
        except ConnectionRefusedError:
            print("ConnectionRefusedError")
            print("Make sure you've started the WebSocket server (here 'node joystick.server.js')")
            print("Also check your proxy settings...")
            sys.exit(1)

    def send(self, status):
        """
        Will send a message corresponding to the joystick's status, as a String.

        :param status: as provided to JoystickReader.start_reading's callback
        :return: None
        """
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

            json_obj = {
                "left": left,
                "right": right,
                "up": up,
                "down": down
            }
            # print("JSON:", json)
            self.ws.send(json.dumps(json_obj))
        except:
            print("Argh!")
            print("Make sure you've started the WebSocket server (here 'node joystick.server.js')")
            print("Also check your proxy settings...")

    def close(self):
        """
        To invoke to release the WS resource.

        :return: None
        """
        print("Closing WS Feeder")
        self.ws.close()
