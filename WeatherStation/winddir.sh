#!/bin/bash
#
CP=./build/libs/WeatherStation-1.0-all.jar
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Dfuzzy.verbose=true"
JAVA_OPTS="$JAVA_OPTS -Dverbose=true"
JAVA_OPTS="$JAVA_OPTS -Dsdl.weather.station.wind.verbose=true"
# JAVA_OPTS="$JAVA_OPTS -client -agentlib:jdwp=transport=dt_socket,server=y,address=1044"
sudo java ${JAVA_OPTS} -cp ${CP} weatherstation.samples.WindDirTest
