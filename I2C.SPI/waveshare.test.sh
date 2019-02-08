#!/bin/bash
CP=./build/libs/I2C.SPI-1.0-all.jar
JAVA_OPTS=
echo -e "Test GPIO Listener"
sudo java -cp $CP $JAVA_OPTS spi.lcd.waveshare.samples.ListenGPIOExample
