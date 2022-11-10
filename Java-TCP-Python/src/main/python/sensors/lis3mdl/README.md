# LIS3MDL

To get started, for Raspberry Pi wiring, see <https://learn.adafruit.com/lis3mdl-triple-axis-magnetometer/python-circuitpython>.  
Unicode characters at <https://pythonforundergradengineers.com/unicode-characters-in-python.html>

## Install the required LIS3MDL Library
```commandline
$ sudo pip3 install adafruit-circuitpython-lis3mdl
```
## Wiring, Raspberry Pi
| LIS3MDL | Raspberry Pi |
|:-------:|:------------:|
|   VIN   |   #1 (3V3)   |
|   GND   |  #14 (GND)   |
| SCL     | #5 (GPIO3 - SCL) |
|   SDA   | #3 (GPIO2 - SDA) |

## Basics: Read raw data
For Python math's methods, see <https://www.w3schools.com/python/module_math.asp>.
```python
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
    print("X:{0:10.2f}, Y:{1:10.2f}, Z:{2:10.2f} \u03BCT, HDG: {3:3.1f}".format(mag_x, mag_y, mag_z, heading))
    print("")
    time.sleep(1.0)

```
> Notice the formula translating the magnetic data to heading (magnetic heading, of course).
 

