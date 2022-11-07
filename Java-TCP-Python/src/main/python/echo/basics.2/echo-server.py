# Adapted from https://realpython.com/python-sockets/
# echo-server.py
# One client at a time

import socket
import signal
import sys

HOST = "127.0.0.1"  # Standard loopback interface address (localhost)
PORT = 65432        # Port to listen on (non-privileged ports are > 1023)


def interrupt(signal, frame):
    print("\nCtrl+C intercepted!")
    sys.exit()


keep_listening: bool = True

signal.signal(signal.SIGINT, interrupt)  # callback defined above.

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, PORT))
    s.listen()
    print("Server is listening. [Ctrl-C] will stop the process.")
    print("Will also terminate if a 'shutdown' is received from a client.")
    while keep_listening:
        conn, addr = s.accept()
        with conn:
            print(f"Connected by client {addr}")
            while True:
                data: bytes = conn.recv(1024)
                if not data:
                    break
                conn.sendall(data)   # Back to the client
                if data.decode().upper() == "SHUTDOWN":
                    print("Shutting down, on client's request.")
                    keep_listening = False
                    break
            print(f"Done with request from {addr}")

print("Exiting server")
