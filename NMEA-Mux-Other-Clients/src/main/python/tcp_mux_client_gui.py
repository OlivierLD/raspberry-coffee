#!/usr/bin/env python3

"""
  - Requires a Mux to be forwarding NMEA data on TCP.
  - Acts as a TCP client, the NMEA Mux is acting as a TCP server, pushing data to all its TCP clients.
  - This example just echoes whatever NMEA sentence is received through the TCP channel.
  - Can be used as a skeleton, for devices accessible from Python (like e-Ink or Papirus, samples available in this repo).

  - Uses TKinter for the GUI. (See https://tkinter.com/, https://realpython.com/python-gui-tkinter/, etc)
"""

import tkinter as tk

import time
import socket
import sys
import traceback
import threading
from typing import List


# There is a thread to receive
# User input is done in the main thread

machine_name: str = "127.0.0.1"  # localhost
tcp_port: int = 7002
verbose: bool = False

CHUNK_SIZE: int = 1024  # To be used by the server too.

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

print("Usage is:")
print(f"python3 {__file__} [{MACHINE_NAME_PRM_PREFIX}{machine_name}] [{PORT_PRM_PREFIX}{tcp_port}] [{VERBOSE_PREFIX}true|false]")
print(f"\twhere {MACHINE_NAME_PRM_PREFIX} and {PORT_PRM_PREFIX} must match the server's settings.\n")

if len(sys.argv) > 0:  # Script name + X args. > 1 should do the job.
    for arg in sys.argv:
        if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
            machine_name = arg[len(MACHINE_NAME_PRM_PREFIX):]
        if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
            tcp_port = int(arg[len(PORT_PRM_PREFIX):])
        if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
            verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")

if verbose:
    print("-- Received from the command line: --")
    for arg in sys.argv:
        print(f"{arg}")
    print("-------------------------------------")

# Create a TCP/IP socket
sock: socket.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# print(f"sock is a {type(sock)}")

# Connect the socket to the port where the server is listening
server_address: tuple = (machine_name, tcp_port)
# print(f"server_address is a {type(server_address)}")
print('connecting to %s port %s' % server_address)
sock.connect(server_address)
print('...Connected')


def help() -> None:
    print("To exit, type Q, QUIT, or EXIT (lower or upper case)")
    print("To see this message again, type H (lower or upper case)")
    print("To pause the continuous display, type P (lower or upper case)")
    print("To resume a paused display, type R (lower or upper case)")
    input(">> Hit return NOW to move on")


help()

window: tk.Tk = tk.Tk()
window.title("NMEA through TCP")
nmea_label: tk.Label = tk.Label(
    text="Just a sec...",
    fg="white",
    bg="black",
    width=50,
    height=10
)
nmea_label.pack()

keep_looping: bool = True
paused: bool = False

RED_ON: str = "\033[031m"
RED_OFF: str = "\033[0m"

NMEA_EOS: str = "\r\n"


def keep_receiving(_socket: socket.socket) -> None:
    while keep_looping:
        # Wait for the response
        data: str = _socket.recv(CHUNK_SIZE).decode("utf-8")
        #
        if not paused:
            if verbose:
                print(f"\treceived '{data.strip()}' ({type(data)})")
            if len(data.strip()) > 0:
                # "\033[031m" + "Hello" + "\033[0m"
                # Split sentences
                sentences: List[str] = data.strip().split(NMEA_EOS)
                if verbose:
                    print(f"{len(sentences)} sentence(s)")
                for sentence in sentences:                      # TODO Display a multiline field ?
                    print(f"Data from MUX: {RED_ON}{sentence.strip()}{RED_OFF}")
                    nmea_label["text"] = sentence.strip()
                    # print(f"Data from MUX: {RED_ON}{data.strip()}{RED_OFF}")  # As received
            else:
                print("Received dummy ping...")
    print("Done with looping thread")


# Here we start the listener thread
try:
    listener: threading.Thread = threading.Thread(name="ClientListener", target=keep_receiving, args=(sock,))
    listener.daemon = True  # Dies on exit
    listener.start()
except Exception as ex:
    print("Exception!")
    traceback.print_exc(file=sys.stdout)

def on_closing() -> None:
    global keep_looping
    global window
    print('closing socket and window')
    # Kill thread
    keep_looping = False
    time.sleep(2)
    sock.close()
    window.destroy()


window.protocol("WM_DELETE_WINDOW", on_closing)
window.mainloop()

