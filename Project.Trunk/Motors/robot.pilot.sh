#!/bin/bash
#
CP=./build/libs/Motors-1.0.jar
#
JAVA_OPTIONS=
#
echo Make sure the server is started \(node robot.server.js\)
ADDR=`hostname -I`
# ADDR=`ifconfig wlan0 2> /dev/null  | awk '/inet addr:/ {print $2}' | sed 's/addr://'`
echo then from your browser, reach http://$ADDR:9876/data/robot.pilot.html, or http://$ADDR:9876/data/mearm.pilot.html
#
sudo java $JAVA_OPTIONS -cp $CP robot.ws.ServoHat
