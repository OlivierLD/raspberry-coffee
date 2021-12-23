#!/bin/bash
#
CP=./build/libs/FONA-1.0-all.jar
#
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dfona.verbose=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dbaud.rate=4800"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dserial.port=/dev/ttyUSB0"
#
sudo java ${JAVA_OPTIONS} -cp ${CP} fona.rxtxmanager.sample.FonaListener
