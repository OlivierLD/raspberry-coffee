#!/bin/bash
CP=./build/libs/I2C.SPI-1.0.jar
OPT=-Darduino.verbose=true
sudo java -cp $CP $OPT i2c.comm.I2CArduino
