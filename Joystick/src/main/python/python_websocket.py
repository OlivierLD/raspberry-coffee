import websocket
import constants

print("Make sure you've started the WebSocket server (here 'node joystick.server.js')")
# from websocket import create_connection
ws = websocket.create_connection("ws://localhost:9876/")

# ws.connect("ws://localhost:9876/") #, http_proxy_host="proxy_host_name", http_proxy_port=3128)

up = False
down = False
left = False
right = False

b = 0x03
if b & constants.JOYSTICK_LEFT == constants.JOYSTICK_LEFT:
    left = True
if b & constants.JOYSTICK_RIGHT == constants.JOYSTICK_RIGHT:
    right = True
if b & constants.JOYSTICK_UP == constants.JOYSTICK_UP:
    up = True
if b & constants.JOYSTICK_DOWN == constants.JOYSTICK_DOWN:
    down = True

json = '"left": {}, "right": {}, "up": {}, "down": {} '.format("true" if left else "false", "true" if right else "false", "true" if up else "false", "true" if down else "false")
json = "{" + json + "}"
print("JSON:", json)

ws.send(json)

ws.close()
print("Done.")
