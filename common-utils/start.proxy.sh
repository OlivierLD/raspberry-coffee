#!/usr/bin/env bash
#
CP=build/libs/common-utils-1.0.jar
JAVA_OPTIONS=
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose.dump=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.client.verbose=true"
#
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.util.logging.config.file=logging.properties"
#
java -cp $CP $JAVA_OPTIONS http.HTTPServer
