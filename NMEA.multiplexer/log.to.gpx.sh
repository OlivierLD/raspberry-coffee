#!/usr/bin/env bash
#
CP=./build/libs/NMEA.multiplexer-1.0.jar
JAVA_OPTIONS=
#
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# use sudo on Raspberry Pi
JAVA_OPTIONS="$JAVA_OPTIONS -Drmc.date.offset=7168"
java $JAVA_OPTIONS -cp $CP util.NMEAtoGPX $1
#
