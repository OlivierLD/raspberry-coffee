import sys
import time
import board
import math
import signal
from typing import List
import busio
import adafruit_lsm303dlh_mag
#
# 1 Gauss = 100 microTesla
# 1 Tesla = 10^4 Gauss
#
# Note: This is the same code (and same I2C address, 0x1E) for the HMC5883L
# Also includes an option for calibration (CLI prm --log-for-calibration:true)
#
LOG_FOR_CAL_PRM_PREFIX: str = "--log-for-calibration:"
VERBOSE_PRM_PREFIX: str = "--verbose:"
log_for_calibration: bool = False
verbose: bool = False
LOG_FILE_NAME: str = "lsm303_log.csv"

# CLI prms management
args: List[str] = sys.argv
if len(args) > 0:  # Script name + X args. > 1 should do the job.
    for arg in args:
        if arg[:len(LOG_FOR_CAL_PRM_PREFIX)] == LOG_FOR_CAL_PRM_PREFIX:
            log_for_calibration = (arg[len(LOG_FOR_CAL_PRM_PREFIX):].lower() == "true")
            print(f"Arg {LOG_FOR_CAL_PRM_PREFIX} now set to {log_for_calibration}.")
        elif arg[:len(VERBOSE_PRM_PREFIX)] == VERBOSE_PRM_PREFIX:
            verbose = (arg[len(VERBOSE_PRM_PREFIX):].lower() == "true")
            print(f"Arg {VERBOSE_PRM_PREFIX} now set to {verbose}.")
        else:
            print(f"Arg {arg} not managed.")

i2c: busio.I2C = board.I2C()  # uses board.SCL and board.SDA
if verbose:
    print(f"Board/I2C is a {type(i2c)}")
sensor: adafruit_lsm303dlh_mag.LSM303DLH_Mag = adafruit_lsm303dlh_mag.LSM303DLH_Mag(i2c)
if verbose:
    print(f"Sensor is a {type(sensor)}")

keep_listening: bool = True


def interrupt(signal, frame):
    global keep_listening
    print("\nCtrl+C intercepted!")
    keep_listening = False
    time.sleep(1.5)
    print("Exiting.")
    # sys.exit()   # DTC


signal.signal(signal.SIGINT, interrupt)  # callback, defined above.

if log_for_calibration:  # Open log file
    log_file = open(LOG_FILE_NAME, "w")   # Overrides if exists.
    log_file.write("rawMagX;rawMagY;rawMagZ;magNorm\n")
    print("Start moving the device in all possible directions, Ctrl-C when done.")

while keep_listening:
    mag_x, mag_y, mag_z = sensor.magnetic
    norm: float = math.sqrt(mag_x ** 2 + mag_y ** 2 + mag_z ** 2)
    # print(f"mag_x:{type(mag_x)}, mag_y:{type(mag_y)}, mag_z:{type(mag_z)}")
    heading: float = math.degrees(math.atan2(mag_y, mag_x))  # Orientation in plan x,y
    while heading < 0:
        heading += 360
    pitch: float = math.degrees(math.atan2(mag_y, mag_z))    # Orientation in plan y,z
    roll: float = math.degrees(math.atan2(mag_x, mag_z))     # Orientation in plan x,z

    # 'magnetic' returns values in micro Tesla (see https://docs.circuitpython.org/projects/lsm303/en/latest/_modules/adafruit_lsm303.html)
    # print('Magnetometer (Gauss): ({0:10.3f}, {1:10.3f}, {2:10.3f}), HDM {3:3.1f}\u00B0'.format(mag_x, mag_y, mag_z, heading))
    print('Magnetometer (\u03BCT): ({0:5.3f}, {1:5.3f}, {2:5.3f}), norm {3:6.3f}, HEAD {4:3.1f}\u00B0, PTCH {5:3.1f}\u00B0, ROLL {6:3.1f}\u00B0'.format(mag_x, mag_y, mag_z, norm, heading, pitch, roll))
    if log_for_calibration:
        log_file.write("{0:f};{1:f};{2:f};{3:f}\n".format(mag_x, mag_y, mag_z, norm))
    # print('')
    if keep_listening:
        time.sleep(1.0 if not log_for_calibration else 0.1)  # Smaller for calibration

if log_for_calibration:  # Close log file
    log_file.close()
    print(f"Check out the log file {LOG_FILE_NAME}.")
print("Bye!")
