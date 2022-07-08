"""
Wiring Check, Pi Radio w/RFM9x

Learn Guide: https://learn.adafruit.com/lora-and-lorawan-for-raspberry-pi
Author: Brent Rubell for Adafruit Industries
"""
import time
import busio
from digitalio import DigitalInOut, Direction, Pull
import board
# Import the SSD1306 module.
import adafruit_ssd1306
# Import the RFM9x radio module.
import adafruit_rfm9x

# Button A
btnA = DigitalInOut(board.D26)    # D5)
btnA.direction = Direction.INPUT
btnA.pull = Pull.UP

# Button B
btnB = DigitalInOut(board.D19)   # D6)
btnB.direction = Direction.INPUT
btnB.pull = Pull.UP

# Button C
btnC = DigitalInOut(board.D13)   # D12)
btnC.direction = Direction.INPUT
btnC.pull = Pull.UP

# Create the I2C interface.
i2c = busio.I2C(board.SCL, board.SDA)

# 128x32 OLED Display
reset_pin = DigitalInOut(board.D4)
display = adafruit_ssd1306.SSD1306_I2C(128, 32, i2c, reset=reset_pin)
# Clear the display.
display.fill(0)
display.show()
width = display.width
height = display.height

# Configure RFM9x LoRa Radio
CS = DigitalInOut(board.CE1)
RESET = DigitalInOut(board.D25)
spi = busio.SPI(board.SCK, MOSI=board.MOSI, MISO=board.MISO)

verbose = False
keep_looping = True
while keep_looping:
    # Clear the image
    display.fill(0)

    try:
        # Button status:
        if verbose:
            print("Button A: {}, Button B: {}, Button C: {}".format(btnA.value, btnB.value, btnC.value))
        # Attempt to set up the RFM9x Module
        try:
            rfm9x = adafruit_rfm9x.RFM9x(spi, CS, RESET, 915.0)
            display.text('RFM9x: Detected', 0, 0, 1)
            if verbose:
                print("RFM9x Detected")
        except RuntimeError as error:
            # Thrown on version mismatch
            display.text('RFM9x: ERROR', 0, 0, 1)
            print('RFM9x Error: ', error)

        # Check buttons
        if not btnA.value:
            # Button A Pressed
            display.text('Ada', width-85, height-7, 1)
            display.show()
            time.sleep(0.1)
        if not btnB.value:
            # Button B Pressed
            display.text('Fruit', width-75, height-7, 1)
            display.show()
            time.sleep(0.1)
        if not btnC.value:
            # Button C Pressed
            display.text('Radio', width-65, height-7, 1)
            display.show()
            time.sleep(0.1)

        display.show()
        time.sleep(0.1)
    except KeyboardInterrupt:
        keep_looping = False

print("\nBye!")
# Clear the image
display.fill(0)
display.show()

print("Done.")
