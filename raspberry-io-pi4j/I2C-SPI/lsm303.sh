#!/bin/bash
CP=./build/libs/I2C-SPI-1.0-all.jar
JAVA_OPTS=
#
# Uncomment when needed:
#
# JAVA_OPTS="${JAVA_OPTS} -Dlsm303.low.pass.filter=false"
# JAVA_OPTS="${JAVA_OPTS} -Dlsm303.verbose=true"
# JAVA_OPTS="${JAVA_OPTS} -Dlsm303.verbose.raw=true"
# JAVA_OPTS="${JAVA_OPTS} -Dlsm303.verbose.mag=true"
# JAVA_OPTS="${JAVA_OPTS} -Dlsm303.verbose.acc=true"
# JAVA_OPTS="${JAVA_OPTS} -Dlsm303.pitch.roll.adjust=true"
# JAVA_OPTS="${JAVA_OPTS} -Dlsm303.log.for.calibration=true"
#
# Can receive the "feature" as cli prm. BOTH, MAGNETOMETER, ACCELEROMETER
sudo java ${JAVA_OPTS} -cp ${CP} i2c.sensor.LSM303 $*

