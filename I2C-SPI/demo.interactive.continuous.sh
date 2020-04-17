#!/bin/bash
CP=./build/libs/I2C-SPI-1.0-all.jar
FREQ_OPTION=
FREQ_OPTION=-Dservo.freq=512
sudo java -cp ${CP} ${FREQ_OPTION} i2c.samples.DemoInteractiveContinuous $*
