#!/bin/bash
#
i2cdetect -y 1
#
I2C_PROVIDER=
# I2C_PROVIDER="-Di2c-provider=raspberrypi-i2c"
#
CP=./build/libs/i2c-spi-2.0-all.jar
java -cp ${CP} ${I2C_PROVIDER} gpio.sensors.i2c.tests.BMP180Test
#
