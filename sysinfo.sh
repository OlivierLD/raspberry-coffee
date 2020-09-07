#!/bin/bash
#
# Move this to your $HOME directory, and modify the FOLDER_NAME variable below accordingly, if needed
# Compare to rpi.status.sh ;)
#
FOLDER_NAME=./raspberry-coffee/common-utils
ARCHIVE_NAME=${FOLDER_NAME}/build/libs/common-utils-1.0.jar
if [[ -f ${ARCHIVE_NAME} ]]
then
  java -cp ${ARCHIVE_NAME} utils.SystemUtils --minimal
else
  pushd ${FOLDER_NAME}
    ../gradlew run -P--minimal
  popd
fi
