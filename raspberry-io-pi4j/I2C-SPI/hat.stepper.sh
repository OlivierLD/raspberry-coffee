#!/bin/bash
CP=./build/libs/I2C-SPI-1.0-all.jar
#
echo -e "Stepper Motor Demo"
echo -e "Similar to StepperTest.py from Adafruit"
#
RPM=30
if [[ $# -eq 1 ]]; then
  RPM=$1
fi
#
echo -e "Revolution per minute set to ${RPM}"
#
OPTS=
OPTS="${OPTS} -Drpm=${RPM}"
OPTS="${OPTS} -Dsteps=200"
OPTS="${OPTS} -Dhat.debug=false"
#
sudo java -cp ${CP} ${OPTS} i2c.samples.motorHAT.StepperDemo
