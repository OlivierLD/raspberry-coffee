# SPDX-FileCopyrightText: 2021 ladyada for Adafruit Industries
# SPDX-License-Identifier: MIT

""" Display magnetometer data once per second """

import time
import math
import board
import adafruit_lis3mdl

i2c = board.I2C()  # uses board.SCL and board.SDA
sensor = adafruit_lis3mdl.LIS3MDL(i2c)

while True:
    mag_x, mag_y, mag_z = sensor.magnetic
    heading: float = math.degrees(math.atan2(mag_y, mag_x))
    while heading < 0:
        heading += 360
    print("X:{0:10.2f}, Y:{1:10.2f}, Z:{2:10.2f} \u03BCT, HDG: {3:3.1f}\u00B0".format(mag_x, mag_y, mag_z, heading))
    # print("")
    time.sleep(1.0)
