#!/bin/bash
CP=./build/libs/I2C-SPI-1.0-all.jar
#
OPTS=-Dhat.debug=false
#
echo motor num \(1..4\) as parameter. Default is 1.
#
sudo java -cp ${CP} ${OPTS} i2c.samples.motorHAT.OneMotorDemo $*
