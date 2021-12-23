#!/bin/bash
#
CP=../build/libs/ADCs-Servos-JoySticks-1.0-all.jar
#
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dmearm.pilot=true"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dtest.only=true"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dno.robot=true"
#
echo Make sure the server is started \(node mearm.server.js\)
# ADDR=`hostname`
ADDR=`ifconfig wlan0 2> /dev/null  | awk '/inet addr:/ {print $2}' | sed 's/addr://'`
echo then from your browser, reach http://$ADDR:9876/data/mearm.pilot.html
#
sudo java ${JAVA_OPTIONS} -cp ${CP} paddle.ws.MeArmWebSocket
