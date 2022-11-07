# From https://realpython.com/python-sockets/
# echo-client.py

import socket

HOST = "127.0.0.1"  # The server's hostname or IP address
PORT = 65432        # The port used by the server

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((HOST, PORT))
    s.sendall(b"Hello, world")  # Send as bytes
    data = s.recv(1024)

print(f"Received {data!r} ({data.decode('utf-8')})")
