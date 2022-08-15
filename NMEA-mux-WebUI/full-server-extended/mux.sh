#!/usr/bin/env bash
#
# Note: Serial ports on MacOS:
# may require Prolific drivers: https://plugable.com/drivers/prolific/
#
OS=`uname -a | awk '{ print $1 }'`
#
MUX_PROP_FILE=nmea.mux.gps.log.properties
if [[ $# -gt 0 ]]; then
  MUX_PROP_FILE=$1
fi
#
echo Using properties file ${MUX_PROP_FILE}
#
JAVA_OPTIONS="${JAVA_OPTIONS}" # From parent script, possibly
#
echo -e "In $0, inherited JAVA_OPTIONS: ${JAVA_OPTIONS}"
#
if [[ "$OS" == "Darwin" ]]; then
  JAVA_OPTIONS="${JAVA_OPTIONS} -Djava.library.path=/Library/Java/Extensions"       # for Mac
fi
if [[ "$OS" == "Linux" ]]; then
  JAVA_OPTIONS="${JAVA_OPTIONS} -Djava.library.path=/usr/lib/jni" # for Raspberry Pi
fi
#
# This variable is used to set the System variable process.on.start.
# (See below).
# It controls ALL the forwarders at once.
#
PROCESS_ON_START=true # Default is true for process.on.start
#
if [[ "${PROCESS_ON_START}" == "false" ]]; then
  MACHINE_NAME=`uname -a | awk '{ print $2 }'`
  PORT=`cat ${MUX_PROP_FILE} | grep http.port=`
  PORT=${PORT#*http.port=}
  echo -e "+-------- N O T E   o n   F O R W A R D E R S ------------------"
  echo -e "| You will need to start the forwarders yourself,"
  echo -e "| invoke PUT http://${MACHINE_NAME}:${PORT}/mux/mux-process/on to start"
  echo -e "| invoke PUT http://${MACHINE_NAME}:${PORT}/mux/mux-process/off to stop"
  echo -e "| Or use http://${MACHINE_NAME}:${PORT}/zip/runner.html from a "
  echo -e "| browser (laptop, cell, tablet...)"
  echo -e "+---------------------------------------------------------------"
fi
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dserial.data.verbose=false"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dtcp.data.verbose=false"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dfile.data.verbose=false"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dws.data.verbose=false"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhtu21df.data.verbose=false"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dbme280.data.verbose=false"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Drnd.data.verbose=false"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dzda.data.verbose=true"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.verbose=true"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Drest.verbose=true"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dmux.data.verbose=true"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dscreen.verbose=true" # Unit changes for SSD1306 (I2C)
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dwith.sun.flower=true"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Ddefault.sf.latitude=37.7489 -Ddefault.sf.longitude=-122.5070" # SF.
#
if [[ "${PROCESS_ON_START}" != "" ]]; then
  JAVA_OPTIONS="${JAVA_OPTIONS} -Dprocess.on.start=$PROCESS_ON_START"
fi
#
JAVA_OPTIONS="${JAVA_OPTIONS} -Dmux.properties=$MUX_PROP_FILE"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dno.ais=false" # Accept AIS Strings
JAVA_OPTIONS="${JAVA_OPTIONS} -Dcalculate.solar.with.eot=true"
#
JAVA_OPTS="${JAVA_OPTS} -DdeltaT=AUTO" # 01-Jan-2019
# For the small USB GPS
JAVA_OPTIONS="${JAVA_OPTIONS} -Drmc.date.offset=7168"
#
# JAVA_OPTIONS="$JAVA_OPTONS -Dpi4j.debug -Dpi4j.linking=dynamic"
#
CP=$(ls ./build/libs/*.jar)
SUDO=
if [[ "$OS" == "Darwin" ]]; then
  CP=${CP}:./libs/RXTXcomm.jar          # for Mac, could need to be tweaked
fi
if [[ "$OS" == "Linux" ]]; then
  CP=${CP}:/usr/share/java/RXTXcomm.jar # For Raspberry Pi
  SUDO="sudo "
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
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# use sudo on Raspberry Pi
# sudo java ${JAVA_OPTIONS} $LOGGING_FLAG $JFR_FLAGS $REMOTE_DEBUG_FLAGS -cp ${CP} nmea.mux.GenericNMEAMultiplexer
# java ${JAVA_OPTIONS} $LOGGING_FLAG $JFR_FLAGS $REMOTE_DEBUG_FLAGS -cp ${CP} nmea.mux.GenericNMEAMultiplexer
# sudo java ${JAVA_OPTIONS} $LOGGING_FLAG $JFR_FLAGS $REMOTE_DEBUG_FLAGS -cp ${CP} navrest.NavServer
COMMAND="${SUDO}java ${JAVA_OPTIONS} ${LOGGING_FLAG} ${JFR_FLAGS} ${REMOTE_DEBUG_FLAGS} -cp ${CP} navserver.ServerWithButtons"
echo -e "Running ${COMMAND}"
${COMMAND}
#
