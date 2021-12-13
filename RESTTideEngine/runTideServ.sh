#!/bin/bash
# Tide REST server
#
cat rest.txt
echo -e "Starting the Tide Rest Server"
#
CP=./build/libs/RESTTideEngine-1.0-all.jar
JAVA_OPTS=
# JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose=true"
JAVA_OPTS="${JAVA_OPTS} -Dtide.verbose=true"
# JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=true"
# JAVA_OPTS="$JAVA_OPTS -Ddata.verbose=true"
export HTTP_PORT=8080
JAVA_OPTS="${JAVA_OPTS} -Dhttp.port=${HTTP_PORT}"
#
JAVA_OPTS="${JAVA_OPTS} -DdeltaT=AUTO"
JAVA_OPTS="${JAVA_OPTS} -Dtide.flavor=SQLITE"
#
# Do a curl http://localhost:${HTTP_PORT}/tide/oplist
# ./oplist.sh &
sleep 10 && \
    echo Invoking http://localhost:${HTTP_PORT}/tide/oplist && \
    curl -X GET http://localhost:${HTTP_PORT}/tide/oplist | jq &
#
echo -e "For basic UI, from a browser, reach http://localhost:${HTTP_PORT}/web/index.html"
#
COMMAND="java -cp ${CP} ${JAVA_OPTS} tiderest.TideServer"
echo -e "Running ${COMMAND}"
${COMMAND}
