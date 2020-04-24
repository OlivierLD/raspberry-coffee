#!/usr/bin/env bash
# List the serial ports, and their owner (if any)
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
java -cp $CP util.SerialUtil
