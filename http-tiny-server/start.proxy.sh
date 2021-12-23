#!/usr/bin/env bash
#
CP=build/libs/http-tiny-server-1.0-all.jar
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose=true"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose.dump=true"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.client.verbose=true"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.super.verbose=true"
#
JAVA_OPTIONS="${JAVA_OPTIONS} -Djava.util.logging.config.file=logging.properties"
#
echo -e "--- Try this: ----------------------------------------------"
echo -e "export http_proxy=http://localhost:10000"
echo -e "export https_proxy=http://localhost:10000"
echo -e "Try doing a curl http://localhost:10000/"
echo -e "       or a curl http://localhost:9999/oplist (assuming a server is running on port 9999)"
echo -e "------------------------------------------------------------"
#
CLASS=utils.proxyguisample.ProxyGUI
# CLASS=http.HTTPServer
java -cp ${CP} ${JAVA_OPTIONS} ${CLASS}
#
