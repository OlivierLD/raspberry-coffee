#!/bin/bash
CP=./build/libs/I2C.SPI-1.0.jar
sudo java -cp $CP -Dverbose=true spi.lcd.nokia.samples.Nokia5110Sample
