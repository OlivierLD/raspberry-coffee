""" Display BMP180 data once per second """

import Adafruit_BMP.BMP085 as BMP085
import time
import signal

# sensor = BMP085.BMP085()
sensor = BMP085.BMP085(busnum=1)

keep_listening: bool = True


def interrupt(signal, frame):
    global keep_listening
    print("\nCtrl+C intercepted!")
    keep_listening = False


signal.signal(signal.SIGINT, interrupt)  # callback, defined above.

while keep_listening:
    print('Temp = {0:0.2f} \u00B0C'.format(sensor.read_temperature()))
    pressure: float = sensor.read_pressure()
    print('Pressure = {0:0.2f} Pa ({1:0.2f} hPa)'.format(pressure, pressure / 100))
    print('Altitude = {0:0.2f} m'.format(sensor.read_altitude()))
    print('SeaLevel Pressure = {0:0.2f} Pa'.format(sensor.read_sealevel_pressure()))
    print("")
    time.sleep(1.0)

print("Bye")
