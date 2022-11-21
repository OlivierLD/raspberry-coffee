#!/bin/bash
echo Read serial port
echo Usage ${0} [BaudRate] \(default 9600\)
echo Try 2400, 4800, 9600, 19200, 38400, 57600, 115200, ...
CP=./classes:$PI4J_HOME/lib/pi4j-core.jar
sudo java -cp $CP readserialport.SerialReader $*

