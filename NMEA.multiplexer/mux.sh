#!/usr/bin/env bash
#
MUX_PROP_FILE=nmea.mux.rpi.demo.properties
if [ $# -gt 0 ]
then
  MUX_PROP_FILE=$1
fi
#
echo Using properties file $MUX_PROP_FILE
#
JAVA_OPTIONS=
# JAVA_OPTIONS="$JAVA_OPTIONS -Djava.library.path=./libs"       # for Mac
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.library.path=/usr/lib/jni" # for Raspberry PI
#
# This variable is used to set the System variable process.on.start.
# (See below).
# It controls ALL the forwarders at once.
#
PROCESS_ON_START=true # Default is true for process.on.start
#
if [ "$PROCESS_ON_START" = "false" ]
then
  MACHINE_NAME=`uname -a | awk '{ print $2 }'`
  PORT=`cat $MUX_PROP_FILE | grep http.port=`
  PORT=${PORT#*http.port=}
  echo -e "+-------- N O T E   o n   F O R W A R D E R S ------------------"
  echo -e "| You will need to start the forwarders yourself,"
  echo -e "| invoke PUT http://$MACHINE_NAME:$PORT/mux-process/on to start"
  echo -e "| invoke PUT http://$MACHINE_NAME:$PORT/mux-process/off to stop"
  echo -e "| Or use http://$MACHINE_NAME:$PORT/web/runner.html from a "
  echo -e "| browser (laptop, cell, tablet...)"
  echo -e "+---------------------------------------------------------------"
fi
#
# JAVA_OPTIONS="$JAVA_OPTIONS -Dserial.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dtcp.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dfile.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dws.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhtu21df.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dbme280.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Drnd.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dzda.data.verbose=true"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose=true"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dmux.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dverbose=false"
JAVA_OPTIONS="$JAVA_OPTIONS -Dprocess.on.start=$PROCESS_ON_START"
#
JAVA_OPTIONS="$JAVA_OPTIONS -Dmux.properties=$MUX_PROP_FILE"
#
# JAVA_OPTIONS="$JAVA_OPTONS -Dpi4j.debug -Dpi4j.linking=dynamic"
#
CP=./build/libs/NMEA.multiplexer-1.0-all.jar
# CP=$CP:./libs/RXTXcomm.jar          # for Mac
CP=$CP:/usr/share/java/RXTXcomm.jar # For Raspberry PI
#
# For JFR
JFR_FLAGS=
# JFR_FLAGS="-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording=duration=10m,filename=nmea.jfr"
# For remote debugging
REMOTE_DEBUG_FLAGS=
# REMOTE_DEBUG_FLAGS="$REMOTE_DEBUG_FLAGS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
#
LOGGING_FLAG=
LOGGING_FLAG=-Djava.util.logging.config.file=./logging.properties
# use sudo on Raspberry PI
sudo java $JAVA_OPTIONS $LOGGING_FLAG $JFR_FLAGS $REMOTE_DEBUG_FLAGS -cp $CP nmea.mux.GenericNMEAMultiplexer
#
