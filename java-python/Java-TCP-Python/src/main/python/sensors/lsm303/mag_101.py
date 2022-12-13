import time
import board
import math
import adafruit_lsm303dlh_mag

i2c = board.I2C()  # uses board.SCL and board.SDA
sensor = adafruit_lsm303dlh_mag.LSM303DLH_Mag(i2c)

while True:
    mag_x, mag_y, mag_z = sensor.magnetic
    heading: float = math.degrees(math.atan2(mag_y, mag_x))
    while heading < 0:
        heading += 360
    print('Magnetometer (gauss): ({0:10.3f}, {1:10.3f}, {2:10.3f}), heading {3:3.3f}'.format(mag_x, mag_y, mag_z, heading))
    # print('')
    time.sleep(1.0)
