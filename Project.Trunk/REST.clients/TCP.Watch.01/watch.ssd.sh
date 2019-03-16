#!/usr/bin/env bash
#
# SSD1306 version
#
CP=./build/libs/TCP.Watch.01-1.0-all.jar
#
# This is the address and port of the logger
# BASE_URL="-Dbase.url=http://localhost:9999"
BASE_URL="-Dbase.url=http://192.168.50.10:9999"
# BASE_URL="-Dbase.url=http://192.168.42.12:9999"
VERBOSE="-Dverbose.00=false -Ddebug=true"
#
LOGGING_FLAG=
LOGGING_FLAG=-Djava.util.logging.config.file=./logging.properties
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -DK1=28 -DK2=29" # Inverted
sudo java -cp $CP $BASE_URL $VERBOSE $JAVA_OPTS $LOGGING_FLAG nmea.tcp.ssd1306_128x64.TCPWatch
