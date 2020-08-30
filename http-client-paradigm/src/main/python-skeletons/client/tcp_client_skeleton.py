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

# Connect the socket to the port where the server is listening
server_address = (machine_name, tcp_port)
print('connecting to %s port %s' % server_address)
sock.connect(server_address)
print('...Connected')

keep_looping = True
# Interactive loop
while keep_looping:
    user_input = input("You say> ")
    if user_input.upper() == 'Q' or user_input.upper() == 'QUIT' or user_input.upper() == 'EXIT':
        keep_looping = False
    else:
        try:
            # Send data
            message = user_input  # 'This is the message.  It will be repeated.'
            # print('sending "%s"' % message)
            sock.sendall(message.encode('utf-8'))
            # Look for the response
            data = sock.recv(1024)
            print('received "%s"' % data.decode("utf-8"))
        except Exception as ex:
            print("Exception: {}".format(ex))
            traceback.print_exc(file=sys.stdout)
        # finally:
        #     print('closing socket')
        #     sock.close()

print('closing socket')
sock.close()
