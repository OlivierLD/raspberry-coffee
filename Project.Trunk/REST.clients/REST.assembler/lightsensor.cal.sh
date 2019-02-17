#!/bin/bash
echo -e "Read an ADC (MPC3008) for 3.3 Volt estimation"
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Ddisplay.digit=false"
JAVA_OPTS="$JAVA_OPTS -Ddebug=false"
CP=./build/libs/REST.assembler-1.0-all.jar
#
echo -e "Usage is $0 --miso:9 --mosi:10 --clk:11 --cs:8 --channel:0"
echo -e " For miso, mosi, clk & cs, use BCM pin numbers"
#
# --clk:18
# --miso:23
# --mosi:24
# --cs:25
# --channel:2
#
sudo java -cp $CP $JAVA_OPTS sensors.MainMCP3008Sample33 $*
#
echo Done.
