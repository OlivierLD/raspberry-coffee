#!/bin/bash
CP=./build/libs/I2C.SPI-1.0-all.jar
# CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
sudo java -cp $CP -Ddemo=false i2c.gui.gyro.GyroscopeUI
