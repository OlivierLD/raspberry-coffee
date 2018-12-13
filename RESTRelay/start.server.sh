#!/bin/bash
CP=./build/libs/RESTRelay-1.0-all.jar
#
JAVA_OPTS="-Dhttp.port=9876"
#
# sudo java -cp $CP httprelay.RelayServer $*
java -cp $CP $JAVA_OPTS httprelay.RelayServer $*
