#!/bin/bash
# Tide REST server
#
cat rest.txt
echo -e "Starting the Tide Rest Server"
#
CP=./build/libs/RESTTideEngine-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Dhttp.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Dtide.verbose=true"
# JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=true"
java -cp ${CP} ${JAVA_OPTS} tiderest.TideServer



