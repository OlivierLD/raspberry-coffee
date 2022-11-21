#!/usr/bin/env bash
# List the serial ports, and their owner (if any)
#
echo -e "--- Known Issue ---"
echo -e "If your are trying to see /dev/ttyACM0, and do not see it, try creating a symbolic link:"
echo -e "sudo ln -s /dev/ttyACM0 /dev/ttyS80 "
echo -e "Also try sudo ${0}"
echo -e "-------------------"
#
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
java -cp ${CP} util.SerialUtil
