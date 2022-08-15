#!/usr/bin/env bash
#
CP=./build/libs/Deviation.Tool-1.0-all.jar
#
echo -e "Convert NMEA to JSON"
echo -e "Usage is $0 [log.life [json.file [declination.to.use]]]"
#
# Default values
FILE_NAME="2010-11-03.Taiohae.nmea"
OUTPUT="data.json"
# Below is the default declination to use if not returned by the NMEA sentences.
DECLINATION="14"
#
if [[ $# -gt 0 ]]; then
  FILE_NAME=$1
fi
#
if [[ $# -gt 1 ]]; then
  OUTPUT=$2
fi
#
if [[ $# -gt 2 ]]; then
  DECLINATION=$3
fi
#
JAVA_OPTIONS="-Ddefault.declination=$DECLINATION"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dlog.file.name=$FILE_NAME"
JAVA_OPTIONS="${JAVA_OPTIONS} -Doutput.file.name=$OUTPUT"
#
echo -e "Processing $FILE_NAME into $OUTPUT"
java -cp ${CP} ${JAVA_OPTIONS} logfile.Processor
#
