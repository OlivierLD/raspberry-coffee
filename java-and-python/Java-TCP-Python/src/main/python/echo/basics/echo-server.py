# From https://realpython.com/python-sockets/
# echo-server.py

import socket

HOST = "127.0.0.1"  # Standard loopback interface address (localhost)
PORT = 65432        # Port to listen on (non-privileged ports are > 1023)

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, PORT))
    s.listen()
    conn, addr = s.accept()
    with conn:
        print(f"Connected by client {addr}")
        while True:
            data = conn.recv(1024)
            if not data:
                break
            conn.sendall(data)   # Back to the client
        print(f"Done with request from {addr}")
