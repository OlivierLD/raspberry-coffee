#!/usr/bin/env bash
#
CP=./build/libs/Deviation.Tool-1.0-all.jar
#
JAVA_OPTIONS="-Ddefault.declination=14"
JAVA_OPTIONS="$JAVA_OPTIONS -Dlog.file.name=2010-11-03.Taiohae.nmea"
JAVA_OPTIONS="$JAVA_OPTIONS -Doutput.file.name=data.json"
#
java -cp $CP $JAVA_OPTIONS logfile.Processor
#
