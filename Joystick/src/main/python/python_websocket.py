import websocket

print("Make sure you've started the WebSocket server (here 'node joystick.server.js'")
# from websocket import create_connection
ws = websocket.create_connection("ws://localhost:9876/")

# ws.connect("ws://localhost:9876/") #, http_proxy_host="proxy_host_name", http_proxy_port=3128)

ws.send("Hello, WS World")

ws.close()
print("Done.")
