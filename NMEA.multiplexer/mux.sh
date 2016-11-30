#!/usr/bin/env bash
#
JAVA_OPTIONS="-Djava.library.path=./libs"
JAVA_OPTIONS="$JAVA_OPTIONS -Dserial.data.verbose=false"
JAVA_OPTIONS="$JAVA_OPTIONS -Dtcp.data.verbose=false"
JAVA_OPTIONS="$JAVA_OPTIONS -Dfile.data.verbose=false"
#
CP=./build/libs/NMEA.multiplexer-1.0.jar
CP="$CP:./libs/RXTXcomm.jar"
java $JAVA_OPTIONS -cp $CP samples.client.mux.GenericNMEAMultiplexer
#
