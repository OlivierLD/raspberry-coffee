#!/bin/bash
CP=./build/libs/I2C.SPI-1.0-all.jar
#
OPTIONS=-Dvl53l0x.debug=false
#
sudo i2cdetect -y 1
#
sudo java -cp ${CP} ${OPTIONS} i2c.sensor.VL53L0X
