#!/bin/bash
CP=./build/libs/I2C.SPI-1.0-all.jar
sudo java -cp $CP i2c.sensor.main.SampleTCS34725Main $*
