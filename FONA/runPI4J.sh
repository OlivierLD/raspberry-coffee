#!/bin/bash
#
CP=./build/libs/FONA-1.0-all.jar
#
JAVA_OPTIONS=
# Initial verbose
JAVA_OPTIONS="$JAVA_OPTIONS -Dverbose=false"
# port definition (default is /dev/ttyAMA0 : 9600)
JAVA_OPTIONS="$JAVA_OPTIONS -Dbaud.rate=4800"
JAVA_OPTIONS="$JAVA_OPTIONS -Dserial.port=/dev/ttyUSB0"
#
echo CP=$CP
#
sudo java $JAVA_OPTIONS -cp $CP fona.pi4jmanager.sample.InteractiveFona

