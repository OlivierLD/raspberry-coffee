#!/bin/bash
CP=./build/libs/I2C.SPI-1.0-all.jar
JAVA_OPTIONS="-Dpca9685.verbose=true"
sudo java -cp $CP $JAVA_OPTIONS i2c.samples.InteractiveServo

