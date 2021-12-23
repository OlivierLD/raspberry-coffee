#!/usr/bin/env bash
#
echo -e "Do not forget to escape the \$ sign..."
#
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
java -cp ${CP} nmea.utils.AutoParser $*
