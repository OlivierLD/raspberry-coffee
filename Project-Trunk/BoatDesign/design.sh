#!/bin/bash
#
# Uncomment if needed...
# See <https://www.baeldung.com/linux/no-x11-display-error>
# export DISPLAY=:0.0
#
CP=./build/libs/BoatDesign-1.0-all.jar
LOGGING_FLAGS="-Djava.util.logging.config.file=./logging.properties"
#
# init.json is the default init-file
#
OPT=
if [[ "$1" != "" ]]; then
  OPT="-Dinit-file=$1"
fi
#
OPT="${OPT} -Dspit-out-points=true"
#
COMMAND="java ${OPT} ${LOGGING_FLAGS} -jar ${CP} $*"
echo -e "Running ${COMMAND}"
${COMMAND}
