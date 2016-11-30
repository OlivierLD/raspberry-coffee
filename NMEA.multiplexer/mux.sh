#!/usr/bin/env bash
#
JAVA_OPTIONS=
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.library.path=./libs"       # for Mac
#JAVA_OPTIONS="$JAVA_OPTIONS -Djava.library.path=/usr/lib/jni" # for Raspberry PI
JAVA_OPTIONS="$JAVA_OPTIONS -Dserial.data.verbose=false"
JAVA_OPTIONS="$JAVA_OPTIONS -Dtcp.data.verbose=false"
JAVA_OPTIONS="$JAVA_OPTIONS -Dfile.data.verbose=false"
JAVA_OPTIONS="$JAVA_OPTIONS -Dws.data.verbose=false"
JAVA_OPTIONS="$JAVA_OPTIONS -Dverbose=false"
#
CP=./build/libs/NMEA.multiplexer-1.0-all.jar
CP=$CP:./libs/RXTXcomm.jar          # for Mac
#CP=$CP:/usr/share/java/RXTXcomm.jar # For Raspberry PI
#
# use sudo on Raspberry PI
java $JAVA_OPTIONS -cp $CP nmeaproviders.client.mux.GenericNMEAMultiplexer
#
