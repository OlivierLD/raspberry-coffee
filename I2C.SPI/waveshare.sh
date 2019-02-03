#!/bin/bash
CP=./build/libs/I2C.SPI-1.0-all.jar
sudo java -cp $CP -Dverbose=true spi.lcd.waveshare.samples.LCD1in3Sample
