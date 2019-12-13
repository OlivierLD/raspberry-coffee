#!/usr/bin/env python3
#
import spidev
import time

# Start SPI protocol.
spi = spidev.SpiDev()
spi.open(0,0) # This is the CE0 Pin (GPIO 08) on the RPi, for CE1, use (0,1)

#Function to read in CE0 channel
def read_spi(channel):
	spidata = spi.xfer2([96, 0]) # sending 2 bytes of data (96 and 0)
	data = ((spidata[0] & 3) << 8) + spidata[1]
	return data


while True:
	try:
		out = read_spi(0)
		if out < 1023:  # Just for safety
			print("Read {}".format(out))
		time.sleep(0.5)
	except KeyboardInterrupt as ki:
		break

print("Bye")
