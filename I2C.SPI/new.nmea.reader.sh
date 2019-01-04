#!/bin/bash
echo -e "Deprecated. See new.pure.nmea.reader.sh"
CP=./build/libs/I2C.SPI-1.0-all.jar
CP=$CP:../../olivsoft/all-libs/nmeaparser.jar
sudo java -cp $CP i2c.sensor.main.SampleBMP180Main
