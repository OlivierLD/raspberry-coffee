#!/usr/bin/env bash
#
# No properties file name is used as parameter here...
MACHINE=RPi
if [ $# -ne 0 ]
then
	echo -e ">>> No properties file argument needed (nor taken). Properties file is hard-coded."
	echo -e "Machine (1st prm) can be Mac or RPi"
	MACHINE=$1
fi
#
echo -e "+------------------------------"
echo -e "| Machine is a $MACHINE"
echo -e "+------------------------------"
#
# MUX_PROP_FILE=nmea.mux.sun.flower.02.properties
#
MUX_PROP_FILE=nmea.mux.sun.flower.03.properties
echo "With $MUX_PROP_FILE, reach the sun.data.html page, on the appropriate port (see properties files, the sun.flower one)"
#
echo Using properties file $MUX_PROP_FILE
#
JAVA_OPTIONS=
if [ "$MACHINE" = "Mac" ]
then
  JAVA_OPTIONS="$JAVA_OPTIONS -Djava.library.path=../Serial.IO/libs" # for Mac
else
  JAVA_OPTIONS="$JAVA_OPTIONS -Djava.library.path=/usr/lib/jni"      # for Raspberry Pi
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
# JAVA_OPTIONS="$JAVA_OPTIONS -Dlsm303.data.verbose=true"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose=true"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dmux.data.verbose=true"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dverbose=false"
#
JAVA_OPTIONS="$JAVA_OPTIONS -Dmux.properties=$MUX_PROP_FILE"
#
# JAVA_OPTIONS="$JAVA_OPTONS -Dpi4j.debug -Dpi4j.linking=dynamic"
#
CP=.
CP=$CP:../GPS.sun.servo/build/libs/GPS.sun.servo-1.0-all.jar  # SolarPanelOrienter lives in this one, must have been built.
CP=$CP:./build/libs/NMEA.multiplexer-1.0-all.jar
# CP=$CP:../SunFlower/build/libs/SunFlower-1.0-all.jar # Included in GPS.sun.servo-1.0-all.jar
#
if [ "$MACHINE" = "Mac" ]
then
  CP=$CP:../Serial.IO/libs/RXTXcomm.jar # for Mac
else
  CP=$CP:/usr/share/java/RXTXcomm.jar   # For Raspberry Pi
fi
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
#
if [ "$MACHINE" = "Mac" ]
then
  # No need to use sudo on Mac
  java $JAVA_OPTIONS $LOGGING_FLAG $JFR_FLAGS $REMOTE_DEBUG_FLAGS -cp $CP nmea.mux.GenericNMEAMultiplexer
else
  # use sudo on Raspberry Pi
  sudo java $JAVA_OPTIONS $LOGGING_FLAG $JFR_FLAGS $REMOTE_DEBUG_FLAGS -cp $CP nmea.mux.GenericNMEAMultiplexer
fi
#
