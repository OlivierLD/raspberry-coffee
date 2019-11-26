#!/usr/bin/env bash
#
CP=./build/libs/NMEA.multiplexer-1.0-all.jar
JAVA_OPTIONS=
#
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# use sudo on Raspberry Pi
# sudo java $JAVA_OPTIONS $LOGGING_FLAG $JFR_FLAGS $REMOTE_DEBUG_FLAGS -cp $CP nmea.mux.GenericNMEAMultiplexer
java ${JAVA_OPTIONS} -cp ${CP} util.LogShrinker $1
#
echo -e "Note >> It is your job to remove unwanted records mentioned above, if you want."
#
