#!/usr/bin/env bash
#
echo -e "Provide the NMEA Strings to parse as CLI parameters."
echo -e "Do not forget to escape the \$ sign..."
#
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
java -cp ${CP} nmea.utils.AutoParser $*
