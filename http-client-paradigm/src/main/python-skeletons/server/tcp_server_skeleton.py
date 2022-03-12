import socket
import sys
import traceback

machine_name: str = "127.0.0.1"
tcp_port: int = 7002
verbose: bool = False

CHUNK_SIZE: int = 1024  # To be re-used by the client.

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

print("Usage is:")
print(f"python3 {__file__} [{MACHINE_NAME_PRM_PREFIX}127.0.0.1] [{PORT_PRM_PREFIX}7002] [{VERBOSE_PREFIX}true|false]\n")

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

# Bind the socket to the port
server_address: tuple = (machine_name, tcp_port)
print('starting up on %s port %s' % server_address)
sock.bind(server_address)

# Listen for incoming connections
sock.listen(1)

nb_messages: int = 0
keep_listening: bool = True
connection: socket.socket
client_address: tuple
while keep_listening:
    # Wait for a connection
    print('waiting for a connection (one at a time)')
    try:
        connection, client_address = sock.accept()
        print(f"connection:{type(connection)}, client_address:{type(client_address)}")
    except KeyboardInterrupt as ki:
        print("\nUser interrupted")
        keep_listening = False
    if keep_listening:
        try:
            print('connection from', client_address)
            # Receive the data in small chunks and retransmit it
            while True:
                try:
                    data: bytes = connection.recv(CHUNK_SIZE)  # Must be in sync with the client (same buffer size)
                    if len(data) > 0:
                        print('received "%s"' % data.decode('utf-8'))
                    # print(f"received '{data}' ({type(data)})")
                    if data:
                        print('Replying to the client')
                        nb_messages += 1
                        response: str = "#{}: {}".format(str(nb_messages), data.decode('utf-8'))
                        connection.sendall(response.encode('utf-8'))
                    else:
                        print('no more data from', client_address)
                        break
                except KeyboardInterrupt as ki:
                    print("User interrupted")
                    keep_listening = False
                    break
        except KeyboardInterrupt as ki_2:
            print("\nUser interrupted")
            keep_listening = False
        except Exception as ex:
            print("Exception!")
            traceback.print_exc(file=sys.stdout)
        finally:
            # Clean up the connection
            print("\tClosing client connection for ", client_address)
            connection.close()
print("TCP Server is now down. Bye.")
