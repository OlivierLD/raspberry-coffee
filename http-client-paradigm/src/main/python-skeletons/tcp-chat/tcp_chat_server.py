import socket
import sys
import traceback
import threading
import json

machine_name: str = "127.0.0.1"
tcp_port: int = 7002
verbose: bool = False

CHUNK_SIZE: int = 1024  # To be re-used by the client.

MACHINE_NAME_PRM_PREFIX: str = "--machine-name:"
PORT_PRM_PREFIX: str = "--port:"
VERBOSE_PREFIX: str = "--verbose:"

SEND_DUMMY_PING: bool = False

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


class ConnectedClient:  # This could be a dict. This class is just another option.
    def __init__(self, name: str, conn: socket.socket, addr: tuple):
        self.name = name
        self.connection = conn
        self.client_address = addr

    def get_name(self):
        return self.name

    def get_connection(self):
        return self.connection

    def get_client_address(self):
        return self.client_address


connected_clients: [ConnectedClient] = []


# TODO Check unicity, or accept same name more than once?
def register_client(name, conn, addr) -> None:
    if verbose:
        print(f"\tCheck if {name} is already in...")
    found: bool = False
    for i in range(0, len(connected_clients)):
        if connected_clients[i].get_name() == name and connected_clients[i].get_client_address() == addr:
            found = True
            if verbose:
                print(f"\t{name} IS already in.")
            break
    if not found:
        if verbose:
            print(f"\tAdding {name} to the list (conn: {type(conn)})")
        connected_clients.append(ConnectedClient(name, conn, addr))


# Create a TCP/IP socket
sock: socket.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Bind the socket to the port
server_address: tuple = (machine_name, tcp_port)
print('starting up on %s port %s' % server_address)
sock.bind(server_address)

# Listen for incoming connections
sock.listen(1)

keep_listening: bool = True


def build_client_list() -> [dict]:
    client_list: [dict] = []
    for i in range(0, len(connected_clients)):
        client_list.append({ 'name': connected_clients[i].get_name() })
    return client_list


def find_who(_conn: socket.socket) -> str:
    who: str = "Not Found"
    for i in range(0, len(connected_clients)):
        if connected_clients[i].get_connection() == _conn:
            who = connected_clients[i].get_name()
            break
    return who


def remove_from_list(_conn: socket.socket) -> str:
    name_to_remove: str = None
    for i in range(0, len(connected_clients)):
        if connected_clients[i].get_connection() == _conn:
            name_to_remove = connected_clients[i].get_name()
            if verbose:
                print(f"\tRemoving [{name_to_remove}] from list")
            connected_clients.pop(i)
            break
    if name_to_remove is None:
        print("Connection to remove was not found...")
    return name_to_remove


#
# This is where you would implement something a bit smarter.
# Just echo.
#
def server_dummy_business(data: bytes, mess_no: int) -> str:
    return data.decode('utf-8')

#
# To be invoked in a thread
#
def manage_client(_connection: socket.socket, _client_address: tuple) -> None:
    global keep_listening
    nb_messages: int = 0
    try:
        if verbose:
            print('\tconnection from', _client_address)
        # Receive the data in small chunks and retransmit them
        while True:
            try:
                data: bytes = _connection.recv(CHUNK_SIZE)  # Must be in sync with the client (same buffer size)
                if len(data) > 0:
                    print('Server Received "%s"' % data.decode('utf-8'))
                # print(f"received '{data}' ({type(data)})")
                if data:
                    print('Server replying to the client')
                    nb_messages += 1
                    # This where you'd implement the feature(s) of your server.
                    # response: str = server_dummy_business(data, nb_messages)
                    request: dict = json.loads(data.decode('utf-8'))
                    # Check if user, connection, client_address are already in
                    register_client(request['user'], _connection, _client_address)

                    if 'request' in request:
                        # If the request if a "request"
                        print("Request!")
                        if request['request'] == 'ClientList':
                            if verbose:
                                print(f"\tSending list back to [{find_who(_connection)}]")
                            clientList:[dict] = build_client_list()
                            finalList: str = f"{json.dumps({ 'client-list': clientList })}"
                            _connection.sendall(finalList.encode('utf-8'))
                        else:
                            print(f"Unknown request {request['request']}...")
                        # _connection.sendall(response.encode('utf-8'))
                    elif 'message' in request:
                        # if the request contains a message
                        destination: str = request['dest']
                        if verbose:
                            print(f"\tMessage for {destination}")
                        # Broadcast to ALL, or dedicated recipient
                        response: str = data.decode('utf-8')
                        for i in range(0, len(connected_clients)):
                            if verbose:
                                print(f"\tConnection is a {type(connected_clients[i].get_connection())}")
                            if destination == 'ALL' or destination == connected_clients[i].get_name():
                                connected_clients[i].get_connection().sendall(response.encode('utf-8'))
                            elif SEND_DUMMY_PING:
                                if verbose:
                                    print(f"\tSending dummy ping to {connected_clients[i].get_name()}")
                                connected_clients[i].get_connection().sendall(''.encode('utf-8'))  # Send empty ping
                        #
                        # _connection.sendall(response.encode('utf-8'))
                    else:
                        print("What kind of stuff is that?")
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
        # Remove from list
        removed: str = remove_from_list(_connection)
        print(f"\tClosing client connection for {_client_address} ({ removed if removed is not None else 'Not Found'})")
        _connection.close()


connection: socket.socket
client_address: tuple

while keep_listening:
    # Wait for a connection
    print('waiting for a connection (multi-threaded)')
    try:
        connection, client_address = sock.accept()
        print(f"Accepted, connection:{type(connection)} {connection}, client_address:{type(client_address)} {client_address}")
        # connection.setblocking(False)
        # Add Connection to list, identified by client_address
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
