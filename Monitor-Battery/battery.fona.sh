#!/usr/bin/env bash
#
CP=./build/libs/Monitor.Battery-1.0-all.jar
OPT=
OPT="$OPT -Dserial.port=/dev/ttyUSB1"
OPT="$OPT -Dbaud.rate=9600"
OPT="$OPT -Dverbose=true"
sudo java -cp $CP $OPT battery.fona.FonaListener
#
