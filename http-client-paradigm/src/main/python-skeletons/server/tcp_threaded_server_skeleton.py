import socket
import sys
import traceback
import threading

machine_name: str = "127.0.0.1"
tcp_port: int = 7002
verbose: bool = False

CHUNK_SIZE: int = 1024  # To be re-used by the client.

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

print("Usage is:")
print(f"python3 {sys.argv[0] if len(sys.argv) >= 0 else __file__} [{MACHINE_NAME_PRM_PREFIX}127.0.0.1] [{PORT_PRM_PREFIX}7002] [{VERBOSE_PREFIX}true|false]")
print(f"  for [{MACHINE_NAME_PRM_PREFIX}], use the machine's IP or name if you want it to be accessible by other machines.\n")

if len(sys.argv) > 0:  # Script name + X args
    for arg in sys.argv:
        # print(f"Managing arg {arg}...")
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

keep_listening: bool = True

#
# This is where you would implement something a bit smarter.
#
def server_business(data: bytes, mess_no: int) -> str:
    return "#{}: {}".format(str(mess_no), data.decode('utf-8'))

#
# To be invoked in a thread
#
def manage_client(_connection: socket.socket, _client_address: tuple) -> None:
    global keep_listening
    nb_messages: int = 0
    try:
        print('connection from', _client_address)
        # Receive the data in small chunks and retransmit them
        while True:
            try:
                data: bytes = _connection.recv(CHUNK_SIZE)  # Must be in sync with the client (same buffer size)
                if len(data) > 0:
                    print('received "%s"' % data.decode('utf-8'))
                # print(f"received '{data}' ({type(data)})")
                if data:
                    print('Replying to the client')
                    nb_messages += 1
                    # This where you'd implement the feature(s) of your server.
                    response: str = server_business(data, nb_messages)
                    #
                    _connection.sendall(response.encode('utf-8'))
                else:
                    print('no more data from', _client_address)
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
        print("\tClosing client connection for ", _client_address)
        _connection.close()


connection: socket.socket
client_address: tuple

while keep_listening:
    # Wait for a connection
    print('waiting for a connection (multi-threaded)')
    try:
        connection, client_address = sock.accept()
        print(f"connection:{type(connection)}, client_address:{type(client_address)}")
    except KeyboardInterrupt as ki:
        print("\nUser interrupted")
        keep_listening = False
    if keep_listening:
        try:
            thread: threading.Thread = threading.Thread(name="Listener", target=manage_client, args=(connection, client_address,))
            thread.daemon = True  # Dies on exit
            thread.start()
        except Exception as ex:
            print("Exception!")
            traceback.print_exc(file=sys.stdout)

print("TCP Server is now down. Bye.")
# sys.exit(0)
