#!/bin/bash
#
# Triple axis compass
#
CP=./build/libs/I2C-SPI-1.0-all.jar
#
# Comment/Uncomment as needed
#
CALIBRATION=
if [[ "$1" == "CAL" ]]
then
  CALIBRATION=true
else
  CALIBRATION=false
fi
#
JAVA_OPTIONS=""
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.verbose=true"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.verbose.raw=true"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.verbose.mag=true"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.low.pass.filter=false"
JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.log.for.calibration=${CALIBRATION}"
#
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhmc5883l.cal.prop.file=hmc5883l.cal.properties"
#
sudo java ${JAVA_OPTIONS} -cp ${CP} i2c.sensor.HMC5883L
