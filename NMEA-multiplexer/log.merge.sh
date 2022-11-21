#!/bin/bash
#
# Merge all the *.nmea file of a given directory into a single one.
#
echo -e "Usage is:"
echo -e "${0} nmea-path final-file-name"
echo -e "It will merge all the *.nmea in {nmea-path} into {final-file-name}"
if [[ $# != 2 ]]; then
  echo -e "Wrong number of parameters: $#, expecting 2."
  exit 1
fi
LOG_PATH=$1
MERGED_FILE_NAME=$2
#
for log in `ls ${LOG_PATH}/*.nmea`; do
  echo -e "Adding ${log}"
  cat ${log} >> $MERGED_FILE_NAME
done
echo -e "Done"
echo -e "Now you might want to run a ./log.shrinker.sh ${MERGED_FILE_NAME}"
