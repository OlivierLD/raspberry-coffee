# -*- coding: utf-8 -*-

import time
import subprocess
import digitalio
import board
from PIL import Image, ImageDraw, ImageFont
import adafruit_rgb_display.st7789 as st7789


# Configuration for CS and DC pins (these are FeatherWing defaults on M0/M4):
cs_pin = digitalio.DigitalInOut(board.CE0)
dc_pin = digitalio.DigitalInOut(board.D25)
reset_pin = None

# Config for display baudrate (default max is 24mhz):
BAUDRATE = 64000000

# Setup SPI bus using hardware SPI:
spi = board.SPI()

# Create the ST7789 display:
disp = st7789.ST7789(
    spi,
    cs=cs_pin,
    dc=dc_pin,
    rst=reset_pin,
    baudrate=BAUDRATE,
    width=135,
    height=240,
    x_offset=53,
    y_offset=40,
)

# Create blank image for drawing.
# Make sure to create image with mode 'RGB' for full color.
height = disp.width  # we swap height/width to rotate it to landscape!
width = disp.height
image = Image.new("RGB", (width, height))
rotation = 90
font_size = 24
font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", font_size)

# Get drawing object to draw on image.
draw = ImageDraw.Draw(image)


def cls(width, height, rotation):
    # Draw a black filled box to clear the image.
    draw.rectangle((0, 0, width, height), outline=0, fill="#000000")  # fill=(0, 0, 0))
    disp.image(image, rotation)


padding = -2
top = padding
bottom = height - padding
# Move left to right keeping track of the current x position for drawing shapes.
x = 0

# Alternatively load a TTF font.  Make sure the .ttf font file is in the
# same directory as the python script!
# Some other nice fonts to try: http://www.dafont.com/bitmap.php

# Turn on the backlight
backlight = digitalio.DigitalInOut(board.D22)
backlight.switch_to_output()
backlight.value = True
keep_looping = True
while keep_looping:
    try:
        # Draw a black filled box to clear the image.
        cls()

        # Write four lines of text.
        y = top
        draw.text((x, y), "Line number one", font=font, fill="#FFFFFF")      # White
        y += font.getsize("-")[1]
        print("Font Height: {}".format(font.getsize("-")[1]))
        draw.text((x, y), "Line number two", font=font, fill="#FFFF00")      # Yellow
        y += font.getsize("-")[1]
        draw.text((x, y), "Line number three", font=font, fill="#00FF00")    # Green
        y += font.getsize("-")[1]
        draw.text((x, y), "Line number four", font=font, fill="#0000FF")     # Blue
        y += font.getsize("-")[1]
        draw.text((x, y), "Line number five", font=font, fill="#FF00FF")     # Purple

        # Display image.
        disp.image(image, rotation)
        time.sleep(0.1)  # Wait for Ctrl-C
    except KeyboardInterrupt:
        print("\n\t\tUser interrupted, exiting.")
        keep_looping = False
        # clean the screen
        cls()
