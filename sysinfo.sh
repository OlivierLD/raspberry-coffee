#!/bin/bash
#
# Move this to your $HOME directory, and modify the pushd command below accordingly, if needed
# Compare to rpi.status.sh ;)
#
ARCHIVE_NAME=./raspberry-coffee/common-utils/build/libs/common-utils-1.0.jar
if [[ -f ${ARCHIVE_NAME} ]]
then
  java -cp ${ARCHIVE_NAME} utils.SystemUtils --minimal
else
  pushd raspberry-coffee/common-utils
    ../gradlew run -P--minimal
  popd
fi
