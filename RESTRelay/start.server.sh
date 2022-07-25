#!/bin/bash
CP=./build/libs/RESTRelay-1.0-all.jar
#
PORT=9876
JAVA_OPTS="-Dhttp.port=${PORT}"
JAVA_OPTS="${JAVA_OPTS} -Drelay.map=1:11"   # See the wiring diagram. #11: BCM:17, GPIO_0
# JAVA_OPTS="${JAVA_OPTS} -Drelay.map=1:11,2:12"   # See the wiring diagram. #11: BCM:17, GPIO_0, #12, BCM: 18, GPIO_1
JAVA_OPTS="${JAVA_OPTS} -Drelay.verbose=true"
#
#
echo -e "Try curl -X GET http://localhost:${PORT}/relay/oplist | jq"
#
# sudo java -cp ${CP} httprelay.RelayServer $*
java -cp ${CP} ${JAVA_OPTS} httpserver.RelayServer $*
