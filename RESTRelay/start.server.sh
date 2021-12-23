#!/bin/bash
CP=./build/libs/RESTRelay-1.0-all.jar
#
JAVA_OPTS="-Dhttp.port=9876"
JAVA_OPTS="${JAVA_OPTS} -Drelay.map=1:11"
JAVA_OPTS="${JAVA_OPTS} -Drelay.verbose=true"
#
# sudo java -cp ${CP} httprelay.RelayServer $*
java -cp ${CP} ${JAVA_OPTS} httprelay.RelayServer $*
