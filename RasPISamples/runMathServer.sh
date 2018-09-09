#!/bin/bash
# Math REST server
#
echo -e "----------------------------"
echo -e "Usage is $0 [-px|--proxy] [-p:|--port:1234] "
echo -e "     -p or --proxy means with a proxy"
echo -e "----------------------------"
#
echo -e "Starting the Math Rest Server"
USE_PROXY=false
HTTP_PORT=
#
for ARG in "$@"
do
	# echo "Managing prm $ARG"
  if [ "$ARG" == "-px" ] || [ "$ARG" == "--proxy" ]
  then
    USE_PROXY=true
  elif [[ $ARG == -p:* ]] || [[ $ARG == --port:* ]] # !! No quotes !!
  then
    HTTP_PORT=${ARG#*:}
    echo -e "Detected port $HTTP_PORT"
  fi
done
#
HTTP_VERBOSE=false
#
CP=./build/libs/RasPISamples-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Dhttp.verbose=$HTTP_VERBOSE"
#
if [ "$USE_PROXY" == "true" ]
then
  echo Using proxy
  JAVA_OPTS="$JAVA_OPTS -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80"
fi
if [ "$HTTP_PORT" != "" ]
then
  JAVA_OPTS="$JAVA_OPTS -Dhttp.port=$HTTP_PORT"
fi
#
echo -e "Using properties:$JAVA_OPTS"
#
java -cp $CP $JAVA_OPTS raspisamples.matrix.server.MathServer
#
