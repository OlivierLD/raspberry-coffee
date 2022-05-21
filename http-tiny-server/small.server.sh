#!/bin/bash
#
# Put this file somewhere in your $PATH
# Modify SERVER_HOME_DIR below to fit your context
# And from anywhere, just run small.server.sh
# This will start a small HTTP server, running in the directory you start it from.
#
# Example:
# - You have run some "gradle test", and you want to see their result on their web page.
# - From the directory you did the "gradle test" from, do a "small.server.sh"
# - From a browser, reach http://localhost:9876/build/reports/tests/test/index.html
#
SERVER_HOME_DIR=~/repos/raspberry-coffee/http-tiny-server/
#
CP=${SERVER_HOME_DIR}build/libs/http-tiny-server-1.0-all.jar
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose.dump=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.client.verbose=false"
#
JAVA_OPTIONS="${JAVA_OPTIONS} -Dstatic.docs=/"
#
JAVA_OPTIONS="${JAVA_OPTIONS} -Djava.util.logging.config.file=${SERVER_HOME_DIR}logging.properties"
#
PORT=9876
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.port=${PORT}"
#
HOSTNAME=$(hostname -I | awk '{ print $1 }' 2>/dev/null) || HOSTNAME=localhost
if [[ "${HOSTNAME}" == "" ]]; then
  HOSTNAME=localhost
fi
#
echo -e "Once started, reach http://${HOSTNAME}:${PORT}/web/index.html or http://${HOSTNAME}:${PORT}/zip/index.html (if web.zip is available)"
echo -e "                                             |"
echo -e "                                             ${PWD}/web/index.html"
echo -e "Running from ${PWD}"
#
java -cp ${CP} ${JAVA_OPTIONS} http.HTTPServer
