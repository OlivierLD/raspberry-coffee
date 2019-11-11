#!/usr/bin/env python3
import serial
import time

port = serial.Serial("/dev/rfcomm0", baudrate=9600)
 
# reading and writing data from and to arduino serially.                                      
# rfcomm0 -> this could be different
data = 0
while True:
  print("DIGITAL LOGIC -- > SENDING...")
  port.write(str.encode(str(data)))
  data = 1 if data == 0 else 0
  rcv = port.readline()
  if rcv:
    print(rcv)
  time.sleep(3)

