#!/usr/bin/env bash
#
# With a Swing UI for the positions.
#
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dspeed.unit=KN"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dsummary=true"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Drmc.date.offset=7168"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Djava.awt.headless=true" # Enforce non-graphical env (no Swing).
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# use sudo on Raspberry Pi
# sudo java ${JAVA_OPTIONS} ${LOGGING_FLAG} ${JFR_FLAGS} ${REMOTE_DEBUG_FLAGS} -cp ${CP} nmea.mux.GenericNMEAMultiplexer
java ${JAVA_OPTIONS} -cp ${CP} util.LogAnalyzer $*
#
