import socket
import sys
import traceback
import json
import threading

# There is a thread to receive
# Send is done in the main thread

machine_name: str = "127.0.0.1"
tcp_port: int = 7002
verbose: bool = False

CHUNK_SIZE: int = 1024  # To be used by the server too.

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

print("Usage is:")
print(f"python3 {__file__} [{MACHINE_NAME_PRM_PREFIX}127.0.0.1] [{PORT_PRM_PREFIX}7002] [{VERBOSE_PREFIX}true|false]")
print(f"\twhere {MACHINE_NAME_PRM_PREFIX} and {PORT_PRM_PREFIX} must match the server's settings.\n")

if len(sys.argv) > 0:  # Script name + X args
    for arg in sys.argv:
        if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
            machine_name = arg[len(MACHINE_NAME_PRM_PREFIX):]
        if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
            tcp_port = int(arg[len(PORT_PRM_PREFIX):])
        if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
            verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")

# Create a TCP/IP socket
sock: socket.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# print(f"sock is a {type(sock)}")

# Connect the socket to the port where the server is listening
server_address: tuple = (machine_name, tcp_port)
# print(f"server_address is a {type(server_address)}")
print('connecting to %s port %s' % server_address)
sock.connect(server_address)
print('...Connected')
print("To exit, type Q, QUIT, or EXIT (lower or upper case)")
print("To get the list of connected clients, type L (lower or upper case)")

keep_looping: bool = True
first_time: bool = True

client_name: str
payload: dict


def keep_receiving(socket: socket.socket) -> None:
    while keep_looping:
        # Wait for the response
        data: str = socket.recv(CHUNK_SIZE).decode("utf-8")
        #
        if verbose:
            print(f"\treceived '{data}' ({type(data)})")
        if len(data.strip()) > 0:
            response: dict = json.loads(data)
            # Process response here
            if 'client-list' in response:
                print(f"{len(response['client-list'])} Client(s):")
                for i in range(len(response['client-list'])):
                    print(f"- Client [{response['client-list'][i]['name']}]")
            else:  # Assume message
                print(f"Message from {response['user']}: {response['message']}")
        else:
            print("Received dummy ping...")


try:
    listener: threading.Thread = threading.Thread(name="ClientListener", target=keep_receiving, args=(sock,))
    listener.daemon = True  # Dies on exit
    listener.start()
except Exception as ex:
    print("Exception!")
    traceback.print_exc(file=sys.stdout)

# Interactive (client->server) loop
while keep_looping:
    if first_time:
        client_name = input("Your name> ")
        first_time = False
    user_input: str = input("You say  > ")
    if user_input.upper() == 'Q' or user_input.upper() == 'QUIT' or user_input.upper() == 'EXIT':
        keep_looping = False
    else:
        try:
            if len(user_input) > 0:   # Message must not be empty. See server implementation.
                if user_input.upper() == 'L':
                    # Client list request
                    if verbose:
                        print("\tClient List Request sent")
                    payload = { 'user': client_name, 'request': 'ClientList' }
                else:
                    message: str = user_input
                    dest_ok: bool = False
                    dest_name: str
                    while not dest_ok:
                        dest_name = input("Dest name> ")  # Say ALL for everyone
                        if len(dest_name.strip()) > 0:
                            dest_ok = True
                        else:
                            print("No empty name please.")
                    # Send data
                    payload = { 'user': client_name, 'message': message, 'dest': dest_name }
                    if verbose:
                        print(f"\tPayload is a {type(payload)}")
                message: str = json.dumps(payload)
                if verbose:
                    print('\tsending "%s"' % message)
                sock.sendall(message.encode('utf-8'))
                #
                # # Wait for the response
                # data: str = sock.recv(CHUNK_SIZE).decode("utf-8")
                # #
                # if verbose:
                #     print(f"\treceived '{data}' ({type(data)})")
                # if len(data.strip()) > 0:
                #     response: dict = json.loads(data)
                #     # Process response here
                #     if 'client-list' in response:
                #         print(f"{len(response['client-list'])} Client(s):")
                #         for i in range(len(response['client-list'])):
                #             print(f"- Client [{response['client-list'][i]['name']}]")
                #     else:  # Assume message
                #         print(f"Message from {response['user']}: {response['message']}")
                # else:
                #     print("Received dummy ping...")
            else:
                print("No empty message please.")
        except Exception as ex:
            print("Exception: {}".format(ex))
            traceback.print_exc(file=sys.stdout)
        # finally:
        #     print('closing socket')
        #     sock.close()

print('closing socket')
sock.close()
