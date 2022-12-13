import time
import board
import math
import signal
import busio
import adafruit_lsm303dlh_mag
#
# 1 Gauss = 100 microTesla
# 1 T = 10^4 G
#

i2c: busio.I2C = board.I2C()  # uses board.SCL and board.SDA
# print(f"Board/I2C is a {type(i2c)}")
sensor: adafruit_lsm303dlh_mag.LSM303DLH_Mag = adafruit_lsm303dlh_mag.LSM303DLH_Mag(i2c)
# print(f"Sensor is a {type(sensor)}")

keep_listening: bool = True


def interrupt(signal, frame):
    global keep_listening
    print("\nCtrl+C intercepted!")
    keep_listening = False
    time.sleep(1.5)
    print("Exiting.")
    # sys.exit()   # DTC


signal.signal(signal.SIGINT, interrupt)  # callback, defined above.

while keep_listening:
    mag_x, mag_y, mag_z = sensor.magnetic
    # print(f"mag_x:{type(mag_x)}, mag_y:{type(mag_y)}, mag_z:{type(mag_z)}")
    heading: float = math.degrees(math.atan2(mag_y, mag_x))
    while heading < 0:
        heading += 360
    # print('Magnetometer (Gauss): ({0:10.3f}, {1:10.3f}, {2:10.3f}), HDM {3:3.1f}\u00B0'.format(mag_x, mag_y, mag_z, heading))
    print('Magnetometer (\u03BCT): ({0:5.3f}, {1:5.3f}, {2:5.3f}), HDM {3:3.1f}\u00B0'.format(mag_x * 100, mag_y * 100, mag_z * 100, heading))
    # print('')
    if keep_listening:
        time.sleep(1.0)
print("Bye!")
