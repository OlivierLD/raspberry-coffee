#!/usr/bin/env bash
#
JAVA_OPTIONS=
# JAVA_OPTIONS="$JAVA_OPTIONS -Djava.library.path=./libs"       # for Mac
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.library.path=/usr/lib/jni" # for Raspberry PI
#
# JAVA_OPTIONS="$JAVA_OPTIONS -Dserial.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dtcp.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dfile.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dws.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhtu21df.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dbme280.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Drnd.data.verbose=false"
#
# JAVA_OPTIONS="$JAVA_OPTIONS -Dmux.data.verbose=false"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dverbose=false"
#
JAVA_OPTIONS="$JAVA_OPTIONS -Dmux.properties=nmea.mux.rpi.demo.properties"
# JAVA_OPTIONS="$JAVA_OPTIONS -Dmux.properties=nmea.mux.3.properties"
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
# use sudo on Raspberry PI
sudo java $JAVA_OPTIONS $JFR_FLAGS $REMOTE_DEBUG_FLAGS -cp $CP nmea.mux.GenericNMEAMultiplexer
#
