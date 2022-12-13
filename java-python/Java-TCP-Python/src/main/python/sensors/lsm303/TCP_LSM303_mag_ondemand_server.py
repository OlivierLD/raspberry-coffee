#!/usr/bin/env python3

"""
A TCP server.

Produces a json object, from the data read from a LSM303 Magnetometer,
on user's request. See method produce_result.
Supports requests like "LISTOP", "GET_LSM303_MAG", "STATUS"
"""

import sys
import signal
import time
import socket
import threading
import traceback
import json
import platform
import board
from datetime import datetime, timezone
from typing import List
import busio
import adafruit_lsm303dlh_mag



keep_listening: bool = True
# print(f"Board/I2C is a {type(i2c)}")
sensor: adafruit_lsm303dlh_mag.LSM303DLH_Mag

HOST: str = "127.0.0.1"  # Standard loopback interface address (localhost). Set to actual IP or name (from CLI) to make it reacheable from outside.
PORT: int = 7001         # Port to listen on (non-privileged ports are > 1023)
verbose: bool = True

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

DATA_EOS: str = "\r\n"  # aka CR-LF


def interrupt(signal, frame):
    global keep_listening
    print("\nCtrl+C intercepted!")
    keep_listening = False
    time.sleep(1.5)
    print("Server Exiting.")
    sys.exit()   # DTC


nb_clients: int = 0


def produce_LSM303_MAG_Data(sensor: adafruit_lsm303dlh_mag.LSM303DLH_Mag) -> str:
    mag_x, mag_y, mag_z = sensor.magnetic
    data: dict = {
        "mag_x": mag_x,
        "mag_y": mag_y,
        "mag_z": mag_z
    }
    data_str: str = json.dumps(data) + DATA_EOS  # DATA_EOS is important, the client does a readLine !
    return data_str


def produce_listop() -> str:
    message: List[str] = [
        "LISTOP",
        "STATUS",
        "GET_LSM303_MAG"
    ]
    payload: str = json.dumps(message) + DATA_EOS
    return payload


def produce_status() -> str:
    global nb_clients
    message: dict = {
        "source": __file__,
        "connected-clients": nb_clients,
        "python-version": platform.python_version(),
        "system-utc-time": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.000Z")
    }
    payload: str = json.dumps(message) + DATA_EOS
    return payload


def produce_result(connection: socket.socket, address: tuple) -> None:
    global nb_clients
    global sensor

    while True:
        users_input: bytes = connection.recv(1024)   # If receive from client is needed... Blocking statement.
        client_mess: str = f"{users_input.decode('utf-8')}".strip().upper()
        data_str: str = ""
        if client_mess == "GET_LSM303_MAG":
            data_str = produce_LSM303_MAG_Data(sensor)
        elif client_mess == "LISTOP":
            data_str = produce_listop()
        elif client_mess == "STATUS":
            data_str = produce_status()
        # elif client_mess == "":
        #     pass  # ignore
        else:
            print(f"Unknown or un-managed message [{client_mess}]")
            data_str = "UN-MANAGED" + DATA_EOS
        if len(client_mess) > 0:
            print(f"Received {client_mess} request.")

        if verbose:
            # Date formatting: https://docs.python.org/2/library/datetime.html#strftime-and-strptime-behavior
            print(f"-- At {datetime.now(timezone.utc).strftime('%d-%b-%Y %H:%M:%S.%f') } --")
            print(f"Sending {data_str.strip()}")
            print("---------------------------")

        try:
            # Send to the client
            if len(data_str) > 0:
                connection.sendall(data_str.encode())
        except BrokenPipeError as bpe:
            print("Client disconnected")
            nb_clients -= 1
            break
        except Exception as ex:
            print("Oops!...")
            traceback.print_exc(file=sys.stdout)
            nb_clients -= 1
            break  # Client disconnected
    print(f"Done with request from {connection}")
    print(f"{nb_clients} {'clients are' if nb_clients > 1 else 'client is'} now connected.")


def main(args: List[str]) -> None:
    global HOST
    global PORT
    global verbose
    global nb_clients
    global sensor

    print("Usage is:")
    print(
        f"python3 {__file__} [{MACHINE_NAME_PRM_PREFIX}{HOST}] [{PORT_PRM_PREFIX}{PORT}] [{VERBOSE_PREFIX}true|false]")
    print(f"\twhere {MACHINE_NAME_PRM_PREFIX} and {PORT_PRM_PREFIX} must match the context's settings.\n")

    if len(args) > 0:  # Script name + X args. > 1 should do the job.
        for arg in args:
            if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
                HOST = arg[len(MACHINE_NAME_PRM_PREFIX):]
            if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
                PORT = int(arg[len(PORT_PRM_PREFIX):])
            if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
                verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")

    if verbose:
        print("-- Received from the command line: --")
        for arg in sys.argv:
            print(f"{arg}")
        print("-------------------------------------")

    signal.signal(signal.SIGINT, interrupt)  # callback, defined above.
    i2c: busio.I2C = board.I2C()  # uses board.SCL and board.SDA
    sensor = adafruit_lsm303dlh_mag.LSM303DLH_Mag(i2c)

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        if verbose:
            print(f"Binding {HOST}:{PORT}...")
        s.bind((HOST, PORT))
        s.listen()
        print("Server is listening. [Ctrl-C] will stop the process.")
        while keep_listening:
            conn, addr = s.accept()
            print(f">> New accept: Conn is a {type(conn)}, addr is a {type(addr)}")
            nb_clients += 1
            print(f"{nb_clients} {'clients are' if nb_clients > 1 else 'client is'} now connected.")
            # Generate JSON data for this client in its own thread.
            client_thread = threading.Thread(target=produce_result, args=(conn, addr,))
            client_thread.daemon = True  # Dies on exit
            client_thread.start()

    print("Exiting server")
    print("Bon. OK.")


if __name__ == '__main__':
    main(sys.argv)
