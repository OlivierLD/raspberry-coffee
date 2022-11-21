#!/usr/bin/env bash
#
# Detects first and last records with speed = 0.
# Tells you what to remove if you want to remove those records.
#
if [[ "$1" == "" ]]; then
  echo -e "Provide the name (full name) of the file to shrink as first parameter, like in ${0} <file-to-shrink>"
  echo -e "Note: this script will tell you what to remove to have a clean log from <file-to-shrink>. IT WILL NOT DO IT FOR YOU."
  exit 1
fi
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
JAVA_OPTIONS=
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# use sudo on Raspberry Pi
# sudo java ${JAVA_OPTIONS} $LOGGING_FLAG $JFR_FLAGS $REMOTE_DEBUG_FLAGS -cp ${CP} nmea.mux.GenericNMEAMultiplexer
java ${JAVA_OPTIONS} -cp ${CP} util.LogShrinker "$1"
#
echo -e "Note >> It is your job to remove unwanted records mentioned above, if you want."
#
