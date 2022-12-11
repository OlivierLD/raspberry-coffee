#!/bin/bash
#
i2cdetect -y 1
#
CP=./build/libs/i2c-spi-2.0-all.jar
java -cp ${CP} gpio.sensors.i2c.tests.BME280Test
#
