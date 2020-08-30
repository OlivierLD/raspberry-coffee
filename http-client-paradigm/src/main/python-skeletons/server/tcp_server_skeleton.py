import socket
import sys
import traceback

machine_name = "127.0.0.1"
tcp_port = 7002
verbose = False

MACHINE_NAME_PRM_PREFIX = "--machine-name:"
PORT_PRM_PREFIX = "--port:"
VERBOSE_PREFIX = "--verbose:"


if len(sys.argv) > 0:  # Script name + X args
    for arg in sys.argv:
        if arg[:len(MACHINE_NAME_PRM_PREFIX)] == MACHINE_NAME_PRM_PREFIX:
            machine_name = arg[len(MACHINE_NAME_PRM_PREFIX):]
        if arg[:len(PORT_PRM_PREFIX)] == PORT_PRM_PREFIX:
            tcp_port = int(arg[len(PORT_PRM_PREFIX):])
        if arg[:len(VERBOSE_PREFIX)] == VERBOSE_PREFIX:
            verbose = (arg[len(VERBOSE_PREFIX):].lower() == "true")


# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Bind the socket to the port
server_address = (machine_name, tcp_port)
print ('starting up on %s port %s' % server_address)
sock.bind(server_address)

# Listen for incoming connections
sock.listen(1)

nb_messages = 0

while True:
    # Wait for a connection
    print('waiting for a connection (one at a time)')
    connection, client_address = sock.accept()

    try:
        print('connection from', client_address)

        # Receive the data in small chunks and retransmit it
        while True:
            data = connection.recv(1024) # Must be in sync with the client (same buffer size)
            print('received "%s"' % data.decode('utf-8'))
            if data:
                print('Replying to the client')
                nb_messages += 1
                response = "#{}: {}".format(str(nb_messages), data.decode('utf-8'))
                connection.sendall(response.encode('utf-8'))
            else:
                print('no more data from', client_address)
                break
    except:
        print("Exception!")
        traceback.print_exc(file=sys.stdout)
    finally:
        # Clean up the connection
        print("\tClosing client connection for ", client_address)
        connection.close()
