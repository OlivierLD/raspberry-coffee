#!/bin/bash
#
# NMEA Version. Position, heading, etc, come from an NMEA input stream.
#
echo Read serial port
echo Usage $0 [BaudRate] \(default 4800\)
echo Try 2400, 4800, 9600, 19200, 38400, 57600, 115200, ...
CP=./build/libs/SunFlower-1.0.jar
sudo java -cp $CP sunservo.SunServoNMEAReader $*
# sudo java -Dserial.port=/dev/ttyUSB0 -cp $CP sunservo.SunServoNMEAReader $*
