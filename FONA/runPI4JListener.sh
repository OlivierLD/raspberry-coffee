#!/bin/bash
#
CP=./build/libs/FONA-1.0-all.jar
#
JAVA_OPTIONS=
JAVA_OPTIONS="$JAVA_OPTIONS -Dbaud.rate=4800"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dbaud.rate=9600"
JAVA_OPTIONS="$JAVA_OPTIONS -Dserial.port=/dev/ttyUSB0"
JAVA_OPTIONS="$JAVA_OPTIONS -Dfona.verbose=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dverbose=true"
#
sudo java $JAVA_OPTIONS -cp $CP fona.pi4jmanager.sample.FonaListener
