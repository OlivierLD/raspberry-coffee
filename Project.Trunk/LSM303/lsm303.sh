#!/bin/bash
#
CP=./build/libs/LSM303-1.0.jar
#
JAVA_OPTIONS=
#
echo Make sure the server is started \(node server.js\)
# ADDR=`hostname`
ADDR=`ifconfig wlan0 2> /dev/null  | awk '/inet addr:/ {print $2}' | sed 's/addr://'`
ADDR=`hostname -I`
echo then from your browser, reach http://$ADDR:9876/data/pitchroll.html
#
sudo java $JAVA_OPTIONS -cp $CP pitchroll.LSM303Reader
