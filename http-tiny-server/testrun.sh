#!/bin/bash
CP=build/libs/http-tiny-server-1.0-all.jar
JAVA_OPTIONS=
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose.dump=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.client.verbose=true"
#
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.util.logging.config.file=logging.properties"
#
PORT=2222
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.port=${PORT}"
#
echo -e "Once started, reach http://localhost:${PORT}/web/index.html or http://localhost:${PORT}/zip/index.html"
#
java -cp $CP $JAVA_OPTIONS http.HTTPServer
