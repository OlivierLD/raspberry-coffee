#!/bin/bash
#
# To put in rc.local when needed:
#   See script raspberry-coffee/I2C-SPI/waveshare.sh
#
CP=./build/libs/I2C-SPI-1.0-all.jar
JAVA_OPTS="-Dwaveshare.1in3.verbose=false"
JAVA_OPTS="${JAVA_OPTS} -Drotation=180"
JAVA_OPTS="${JAVA_OPTS} -Dwaveshare.verbose=true"
sudo java -cp ${CP} ${JAVA_OPTS} spi.lcd.waveshare.samples.LCD1in3Sample
