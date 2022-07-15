#!/bin/bash
CP=./build/libs/diozero-101-1.0-all.jar
#
REMOTE_DEBUG_FLAGS=
# Make sure you have suspend=y below, for this kind of app.
# suspend=y ill wait for the debugger to connect before moving on.
REMOTE_DEBUG_FLAGS="${REMOTE_DEBUG_FLAGS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
if [[ "${REMOTE_DEBUG_FLAGS}" != "" ]]; then
  echo -e "Will use remote debug this prms ${REMOTE_DEBUG_FLAGS}"
fi
# java -jar ${CP} ${REMOTE_DEBUG_FLAGS}
sudo java -jar ${CP} ${REMOTE_DEBUG_FLAGS}
