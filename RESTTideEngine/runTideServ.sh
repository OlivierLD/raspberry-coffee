#!/bin/bash
# Tide REST server
#
cat rest.txt
echo -e "Starting the Tide Rest Server"
echo -e "Also try:"
echo -e "$0 --verbose --browser --oplist:false --flavor:SQLITE"
#
CP=./build/libs/RESTTideEngine-1.0-all.jar
JAVA_OPTS=
# JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose=true"
# JAVA_OPTS="${JAVA_OPTS} -Dtide.verbose=true" # See below - script prm management.
# JAVA_OPTS="${JAVA_OPTS} -Dastro.verbose=true"
# JAVA_OPTS="${JAVA_OPTS} -Ddata.verbose=true"
export HTTP_PORT=8080
JAVA_OPTS="${JAVA_OPTS} -Dhttp.port=${HTTP_PORT}"
#
JAVA_OPTS="${JAVA_OPTS} -DdeltaT=AUTO"
# Default flavor is XML
# JAVA_OPTS="${JAVA_OPTS} -Dtide.flavor=SQLITE"
# JAVA_OPTS="${JAVA_OPTS} -Ddb.path=other.db"  # Overrides the default sql/tides.db
# JAVA_OPTS="${JAVA_OPTS} -Dtide.flavor=JSON"
#
OPEN_BROWSER=false
OP_LIST=true
# Process script args
for ARG in "$@"
do
	echo -e "Managing prm ${ARG}"
  if [[ ${ARG} == "--flavor:"* ]]
	then
	  FLAVOR=${ARG#*:}
    JAVA_OPTS="${JAVA_OPTS} -Dtide.flavor=${FLAVOR}"
  elif [[ ${ARG} == "--oplist:"* ]]
	then
	  OP_LIST=${ARG#*:}
	elif [[ "$ARG" == "-v" ]] || [[ "$ARG" == "--verbose" ]]
	then
    JAVA_OPTS="${JAVA_OPTS} -Dtide.verbose=true"
	elif [[ "$ARG" == "-b" ]] || [[ "$ARG" == "--browser" ]]
	then
	  OPEN_BROWSER=true
  else
    echo -e "Parameter ${ARG} not managed."
    echo -e "See the script $0 for details."
  fi
done
#
# Do a curl http://localhost:${HTTP_PORT}/tide/oplist
if [[ "${OP_LIST}" == "true" ]]
then
  # ./oplist.sh &
  sleep 10 && \
      echo Invoking http://localhost:${HTTP_PORT}/tide/oplist && \
      curl -X GET http://localhost:${HTTP_PORT}/tide/oplist | jq &
fi
#
echo -e "For basic UI, from a browser, reach http://localhost:${HTTP_PORT}/web/index.html"
if [[ "${OPEN_BROWSER}" == "true" ]]
then
  XDG=$(which xdg-open)
  if [[ "${XDG}" != "" ]]
  then
    sleep 10 && \
      echo -e "Opening 'http://localhost:${HTTP_PORT}/web/index.html' a browser..." && \
      xdg-open http://localhost:${HTTP_PORT}/web/index.html &
  else
    echo -e "xdg-open not found on this system..."
  fi
fi
#
COMMAND="java -cp ${CP} ${JAVA_OPTS} tiderest.TideServer"
echo -e "Running ${COMMAND}"
${COMMAND}
