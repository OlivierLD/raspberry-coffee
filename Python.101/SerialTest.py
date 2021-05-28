# http://www.elinux.org/Serial_port_programming
# sudo apt-get install python-serial
# or
# pip install pyserial
#
import serial

VERBOSE = False

def read_line_CR(port: serial.serialposix.Serial) -> str:
    rv = []   # bytearray()
    while True:
        ch = port.read()
        if VERBOSE:
            print(f"Read from Serial: {ch} (type {type(ch)})")
        rv.append(ch)
        # if ch == b'\r' or ch == b'':
        if ch == b'\n':
            serial_string = "".join(map(bytes.decode, rv))
            if VERBOSE:
                print(f"\tReturning {serial_string}")
            return serial_string


port_name = "/dev/tty.usbmodem141101"
baud_rate = 4800
# port_name = "/dev/ttyACM0"
# baud_rate = 115200
#
port = serial.Serial(port_name, baudrate=baud_rate, timeout=3.0)
print(f"Serial Port type:{type(port)}")
print("Let's go")
read_only = True
while True:
    if not read_only:
        port.write("\r\nSay something:")
        rcv = read_line_CR(port)
        port.write("\r\nYou said:" + repr(rcv))
    else:
        rcv = read_line_CR(port)
        print(f"Received: {repr(rcv)}")
        # print(f"Received: {rcv}")
