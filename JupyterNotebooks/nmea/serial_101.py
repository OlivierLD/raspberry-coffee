#!/usr/bin/env python3
#
#  Read a serial port, spit it out
#  Run ./serial_101.py
#
# May require:
# pip install pyserial
#
# Also see:
# - https://pythonhosted.org/pyserial/pyserial.html
# - https://pyserial.readthedocs.io/en/latest/pyserial.html
#
import serial

DEBUG = False

# For tests
# On mac, USB GPS on port /dev/tty.usbmodem14101,
# Raspberry Pi, use /dev/ttyUSB0 or so.
port_name = "/dev/tty.usbmodem14101"
# port_name = "/dev/ttyS80"
baud_rate = 4800
port = serial.Serial(port_name, baudrate=baud_rate, timeout=3.0)
print("Let's go. Hit Ctrl+C to stop")
#
while True:
    try:
        ch = port.read()
        print("{:02x} ".format(ord(ch)), end='', flush=True)
    except KeyboardInterrupt as ki:
        break

print("\n\t\tUser interrupted, exiting.")
port.close()

print("Bye.")
