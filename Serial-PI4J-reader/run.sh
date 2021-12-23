#!/bin/bash
echo Read serial port, raw data
#
CP=./build/libs/Serial-PI4J-reader-1.0-all.jar
JAVA_OPTIONS=
#
# For USB, use /dev/ttyUSB0 at 4800,
# For the UART interface, use /dev/ttyAMA0 at 9600
# Default serial port is /dev/ttyAMA0
#JAVA_OPTIONS="${JAVA_OPTIONS} -Dport.name=/dev/ttyUSB0"
#JAVA_OPTIONS="${JAVA_OPTIONS} -Dbaud.rate=4800"
#
JAVA_OPTIONS="${JAVA_OPTIONS} -Dport.name=/dev/serial0"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dbaud.rate=38400"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=true"
sudo java -cp ${CP} ${JAVA_OPTIONS} readserialport.SerialDataReader
