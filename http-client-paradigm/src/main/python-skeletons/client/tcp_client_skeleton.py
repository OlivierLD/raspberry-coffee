import socket
import sys
import traceback

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

keep_looping: bool = True
# Interactive loop
while keep_looping:
    user_input: str = input("You say> ")
    if user_input.upper() == 'Q' or user_input.upper() == 'QUIT' or user_input.upper() == 'EXIT':
        keep_looping = False
    else:
        try:
            # Send data
            message: str = user_input  # 'This is the message.  It will be repeated.'
            # print('sending "%s"' % message)
            sock.sendall(message.encode('utf-8'))
            # Look for the response
            data: str = sock.recv(CHUNK_SIZE).decode("utf-8")
            print(f"received '{data}' ({type(data)})")
        except Exception as ex:
            print("Exception: {}".format(ex))
            traceback.print_exc(file=sys.stdout)
        # finally:
        #     print('closing socket')
        #     sock.close()

print('closing socket')
sock.close()
