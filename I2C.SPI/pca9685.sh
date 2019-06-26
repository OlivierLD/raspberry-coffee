#!/bin/bash
CP=./build/libs/I2C.SPI-1.0.jar
sudo java -cp $CP i2c.servo.pwm.PCA9685 $*
