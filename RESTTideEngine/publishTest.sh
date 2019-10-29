#!/bin/bash
# Test the publisher
CP=./build/libs/RESTTideEngine-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Dtide.verbose=true"
# JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=true"
java -cp ${CP} ${JAVA_OPTS} tideengine.publisher.TidePublisher

