#!/bin/bash
CP=./build/libs/I2C.SPI-1.0.jar
JAVA_OPTS="-Dwaveshare.1in3.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Drotation=180"
sudo java -cp $CP $JAVA_OPTS spi.lcd.waveshare.samples.LCD1in3Sample
