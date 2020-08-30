## HTTP Client Samples
This module refers to its sibling `http-client-paradigm`.

We will show here how to use several sensors and screens, managed from Python,
having their features exposed through an HTTP server - written in Python as well -
so they can be reached from any HTTP Client, written in Java in our case.

## Featuring
- [LIS3MDL](https://www.adafruit.com/product/4479), tutorial [here](https://learn.adafruit.com/lis3mdl-triple-axis-magnetometer).
- [Monochrome 2.42" oled screen](https://learn.adafruit.com/1-5-and-2-4-monochrome-128x64-oled-display-module?view=all), Raspberry Pi, `I2C` and `SPI`.
- [240x240 TFT](https://www.adafruit.com/product/3787), [tutorial](https://learn.adafruit.com/adafruit-1-3-and-1-54-240-x-240-wide-angle-tft-lcd-displays?view=all), look for `ST7789 and ST7735-based Displays, 1.3", 1.54", and 2.0" IPS TFT Display`.

### LIS3MDL
Install the required python packages
```
$ sudo pip3 install adafruit-circuitpython-lis3mdl
```
Then, on the Raspberry Pi with the `LIS3MDL` connected to it: 
```
$ cd src/main/python/lis3mdl/server
$ python3 lis3mdl_server.py --machine-name:$(hostname -I) [ --port:8888 --verbose:true ]
```
After that, from anywhere on the same network:
```
$  ./read_mag.sh 
  Ctrl+C to stop
  Heading: 148.411910 Pitch: 125.781553, Roll: -113.902522
  Heading: 148.411910 Pitch: 125.781553, Roll: -113.902522
  Heading: 148.411910 Pitch: 125.781553, Roll: -113.902522
  Heading: 148.411910 Pitch: 125.781553, Roll: -113.902522
  Heading: 148.411910 Pitch: 125.781553, Roll: -113.902522
  Heading: 148.411910 Pitch: 125.781553, Roll: -113.902522
. . .
```
The URL of the server can be modified in the `read_mag.sh` script. 

---
 