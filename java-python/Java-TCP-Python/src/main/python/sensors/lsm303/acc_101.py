import time
import board
import adafruit_lsm303_accel

i2c = board.I2C()  # uses board.SCL and board.SDA
sensor = adafruit_lsm303_accel.LSM303_Accel(i2c)

while True:
    acc_x, acc_y, acc_z = sensor.acceleration

    print('Acceleration (m/s^2): ({0:10.3f}, {1:10.3f}, {2:10.3f})'.format(acc_x, acc_y, acc_z))
    # print('')
    time.sleep(1.0)