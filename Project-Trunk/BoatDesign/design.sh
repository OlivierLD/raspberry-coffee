#!/bin/bash
CP=./build/libs/BoatDesign-1.0-all.jar
LOGGING_FLAG="-Djava.util.logging.config.file=./logging.properties"
#
OPT=
if [[ "$1" != "" ]]
then
  OPT="-Dinit-file=$1"
fi
#
OPT="${OPT} -Dspit-out-points=true"
#
COMMAND="java ${OPT} ${LOGGING_FLAG} -jar ${CP} $*"
echo -e "Running ${COMMAND}"
${COMMAND}
