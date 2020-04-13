#!/bin/bash
CP=./build/libs/I2C.SPI-1.0-all.jar
sudo java -cp ${CP} spi.sensor.main.SampleBMP183Main
