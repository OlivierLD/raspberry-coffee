#!/usr/bin/env python3
#
#  Read a serial port, spit it out
#  Run ./serial_102.py --detect to detect start and end of sequences
#
# May require:
# pip install pyserial
#
# Also see:
# - https://pythonhosted.org/pyserial/pyserial.html
# - https://pyserial.readthedocs.io/en/latest/pyserial.html
#
import serial
import sys

DEBUG = False
DETECT_SENTENCE = False  # Turn this to True to detect start and end of sequences
DISPLAY_SENTENCE = False  # Turn this to True to display the detected sentence, in ASCII

for i in range(len(sys.argv)):
    print("{}".format(sys.argv[i]))
    if sys.argv[i] == "--detect":
        DETECT_SENTENCE = True
    if sys.argv[i] == "--display":
        DISPLAY_SENTENCE = True

# For tests
# On mac, USB GPS on port /dev/tty.usbmodem14101,
# Raspberry Pi, use /dev/ttyUSB0 or so.
port_name = "/dev/tty.usbmodem14101"
# port_name = "/dev/ttyS80"
baud_rate = 4800
port = serial.Serial(port_name, baudrate=baud_rate, timeout=3.0)
print("Let's go. Hit Ctrl+C to stop")
previous_char = ''
rv = []
#
while True:
    try:
        ch = port.read()
        if DETECT_SENTENCE:
            if ord(ch) == ord('$'):
                print("\nNew sentence?")
        print("{:02x} ".format(ord(ch)), end='', flush=True)
        rv.append(ch)
        if DETECT_SENTENCE:
            if ord(ch) == 0x0A and ord(previous_char) == 0x0D:
                string = "".join(map(bytes.decode, rv))
                print("\nEnd of sentence {}".format(string if DISPLAY_SENTENCE else ""))
                rv = []
        previous_char = ch
    except KeyboardInterrupt as ki:
        break

print("\n\t\tUser interrupted, exiting.")
port.close()

print("Bye.")
