#!/bin/bash
CP=./build/libs/I2C.SPI-1.0-all.jar
JAVA_OPTS="-Dwaveshare.1in3.verbose=true"
sudo java -cp $CP $JAVA_OPTS spi.lcd.waveshare.samples.LCD1in3Sample
