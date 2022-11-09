import Adafruit_BMP.BMP085 as BMP085

sensor = BMP085.BMP085()

print('Temp = {0:0.2f} \u00B0C'.format(sensor.read_temperature()))
print('Pressure = {0:0.2f} Pa'.format(sensor.read_pressure()))
print('Altitude = {0:0.2f} m'.format(sensor.read_altitude()))
print('SeaLevel Pressure = {0:0.2f} Pa'.format(sensor.read_sealevel_pressure()))
