#!/bin/bash
#
# Move this to your $HOME directory, and modify the FOLDER_NAME variable below accordingly, if needed
# Compare to rpi.status.sh ;)
#
echo -e "All Raspberry Pi Models' description available at https://www.raspberrypi.org/products/"
echo -e ""
FOLDER_NAME=~/raspberry-coffee/common-utils
ARCHIVE_NAME=${FOLDER_NAME}/build/libs/common-utils-1.0-all.jar
if [[ -f ${ARCHIVE_NAME} ]]
then
  java -cp ${ARCHIVE_NAME} utils.SystemUtils --minimal --no-free-mem
else
  pushd ${FOLDER_NAME}
    ../gradlew run -P--minimal -P--no-free-mem
  popd
fi
#
echo -e "-------------------------"
echo -e "uname -m: $(uname -m)"
echo -e "-------------------------"
lscpu
echo -e "-------------------------"
echo -e "Architecture: $(getconf LONG_BIT) bits"
echo -e "-------------------------"
file /lib/systemd/systemd
echo -e "-------------------------"
