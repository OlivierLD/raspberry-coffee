# BMP180
> Warning: This sensor has been discontinued.

To get started, for Raspberry Pi wiring, see <https://learn.adafruit.com/using-the-bmp085-with-raspberry-pi>.  
Unicode characters at <https://pythonforundergradengineers.com/unicode-characters-in-python.html>

## Install required library(ies)
```commandline
sudo apt-get update
sudo apt-get install git build-essential python-dev python-smbus
```

```commandline
git clone https://github.com/adafruit/Adafruit_Python_BMP.git
cd Adafruit_Python_BMP
sudo python setup.py install
```

## Wiring, Raspberry Pi
| BMP180 |   Raspberry Pi   |
|:------:|:----------------:|
|  VIN   |     #1 (3V3)     |
|  GND   |    #14 (GND)     |
|  SCL   | #5 (GPIO3 - SCL) |
|  SDA   | #3 (GPIO2 - SDA) |

## Basics: Read raw data
```python
import Adafruit_BMP.BMP085 as BMP085

# sensor = BMP085.BMP085()
sensor = BMP085.BMP085(busnum=1)

print('Temp = {0:0.2f} \u00B0C'.format(sensor.read_temperature()))
print('Pressure = {0:0.2f} Pa'.format(sensor.read_pressure()))
print('Altitude = {0:0.2f} m'.format(sensor.read_altitude()))
print('SeaLevel Pressure = {0:0.2f} Pa'.format(sensor.read_sealevel_pressure()))

```