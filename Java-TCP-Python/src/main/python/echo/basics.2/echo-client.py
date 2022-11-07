# Adapted from https://realpython.com/python-sockets/
# echo-client.py

import socket

HOST: str = "127.0.0.1"  # The server's hostname or IP address
PORT: int = 65432        # The port used by the server

keep_looping: bool = True

print("Type the String to send to the server (finish with a [return]), ")
print("Or Q, QUIT, or EXIT to finish the program.")

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((HOST, PORT))
    print("Client connected")
    while keep_looping:
        user_input: str = input()  # Blind input
        if user_input.upper() == 'Q' or user_input.upper() == 'QUIT' or user_input.upper() == 'EXIT':
            keep_looping = False
        else:
            s.sendall(user_input.encode())  # Send as bytes
            data: bytes = s.recv(1024)
            # print(f"Data is a {type(data)}")
            print(f"Received {data.decode('utf-8')}")

print("Client disconnected")
