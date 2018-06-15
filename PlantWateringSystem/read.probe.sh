#!/usr/bin/env bash
CP=build/libs/PlantWateringSystem-1.0-all.jar
#
echo "Usage is $0 [debug]"
echo "   Use 'debug' to remote-debug from another machine."
#
VERBOSE=false
JAVA_OPTIONS="-Dsth.debug=$VERBOSE"
#
if [ "$1" == "debug" ]
then
  # For remote debugging:
  JAVA_OPTIONS="$JAVA_OPTIONS -client -agentlib:jdwp=transport=dt_socket,server=y,address=4000"
fi
# For remote JVM Monitoring
# JAVA_OPTIONS="$JAVA_OPTIONS -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=raspberrypi-boat"
#
# Use program argument --help for help.
#
# verbose: ANSI, STDOUT, NONE
USER_PRM="--verbose:ANSI"
USER_PRM="$USER_PRM --water-below:35 --water-during:10 --resume-after:120"
USER_PRM="$USER_PRM --with-rest-server:true --http-port:8088"
# USER_PRM="$USER_PRM --water-below:50 --water-during:10 --resume-after:120"
java $JAVA_OPTIONS -cp $CP main.STH10 $USER_PRM
