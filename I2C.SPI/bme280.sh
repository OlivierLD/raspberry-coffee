#!/bin/bash
CP=./build/libs/I2C.SPI-1.0.jar
#
OPTS=-Dbme280.debug=true
#
sudo java -cp $CP $OPTS i2c.sensor.BME280
