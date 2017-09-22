# http://www.elinux.org/Serial_port_programming
# sudo apt-get install python-serial
#
import serial
import time

def readlineCR(port):
	rv = ""
	while True:
		ch = port.read()
		rv += ch
		if ch=='\r' or ch=='':
			return rv

port = serial.Serial("/dev/ttyAMA0", baudrate=115200, timeout=3.0)

while True:
	port.write("\r\nSay something:")
	rcv = readlineCR(port)
	port.write("\r\nYou sent:" + repr(rcv))
