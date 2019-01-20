#!/bin/bash
echo -e "Read an ADC (MPC3008) for 3.3 Volt estimation"
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Ddisplay.digit=false"
JAVA_OPTS="$JAVA_OPTS -Ddebug=false"
CP=./build/libs/PlantWateringSystem-1.0-all.jar
#
echo -e "Usage is $0 --miso:9 --mosi:10 --clk:11 --cs:8 --channel:0"
echo -e " For miso, mosi, clk & cs, use BCM pin numbers"
#
sudo java -cp $CP $JAVA_OPTS sensors.sparkfunsoilhimuditysensor.MainMCP3008Sample33 $*
#
echo Done.
