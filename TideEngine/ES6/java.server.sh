#!/bin/bash
echo -e "Starting java server"
# CP=$(find ~/repos/raspberry-coffee -name http-tiny-server-1.0-all.jar)
JAR_FILE=../../http-tiny-server/build/libs/http-tiny-server-1.0-all.jar
if [[ ! -f ${JAR_FILE} ]]; then
    echo -e "${JAR_FILE} not found where expected. Exiting"
    exit 1
fi
HTTP_PORT=8080
CP=${JAR_FILE}
JAVA_OPTIONS="${JAVA_OPTIONS} -Dautobind=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dwith.rest=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose=${VERBOSE}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.port=${HTTP_PORT}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dstatic.docs=/"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.super.verbose=true"
echo -e "Will run: java -cp ${CP} ${JAVA_OPTIONS} http.HTTPServer &"
java -cp ${CP} ${JAVA_OPTIONS} http.HTTPServer &
SERVER_PROCESS_ID=$(echo $!)
echo -e "To kill the server, used PID ${SERVER_PROCESS_ID}"
