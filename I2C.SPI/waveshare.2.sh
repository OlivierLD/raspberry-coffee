#!/bin/bash
#
echo -e "Use K1 and K3 (or Joystick Up and Down) to scroll through screens"
#
CP=./build/libs/I2C.SPI-1.0-all.jar
JAVA_OPTS="-Dwaveshare.1in3.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Dfont.size=8"
JAVA_OPTS="$JAVA_OPTS -Drotation=180"
sudo java -cp $CP $JAVA_OPTS spi.lcd.waveshare.samples.InteractiveScreenSample
