#!/bin/bash
CP=./build/libs/I2C.SPI-1.0.jar
#
OPTS=-Dhat.debug=false
#
sudo java -cp $CP $OPTS i2c.samples.motorHAT.RobotDemo
