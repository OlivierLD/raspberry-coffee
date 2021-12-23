#!/bin/bash
#
CP=
CP=${CP}:../build/libs/Weather-Station-Implementation-1.0-all.jar
#
JAVA_OPTS=
JAVA_OPTS="${JAVA_OPTS} -Dsimulator.verbose=false"
#
DATA_LOGGERS=
#
# DATA_LOGGERS="$DATA_LOGGERS,weatherstation.logger.DummyLogger"
DATA_LOGGERS="$DATA_LOGGERS,weatherstation.logger.NMEAOverTCPLogger"
#
JAVA_OPTS="${JAVA_OPTS} -Dweather.station.verbose=false"
#
JAVA_OPTS="${JAVA_OPTS} -Dhttp.port=8080"
JAVA_OPTS="${JAVA_OPTS} -Dsnap.verbose=true"
#
DATA_LOGGERS="$DATA_LOGGERS,weatherstation.logger.HTTPLogger"
# Also -Dws.uri if needed
# DATA_LOGGERS="$DATA_LOGGERS,weatherstation.logger.WebSocketLogger"
JAVA_OPTS="${JAVA_OPTS} -Ddata.logger=$DATA_LOGGERS"
JAVA_OPTS="${JAVA_OPTS} -Dstation.lat=37.7489 -Dstation.lng=-122.5070"
# JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose=true"
JAVA_OPTS="${JAVA_OPTS} -Dtcp.verbose=false"
JAVA_OPTS="${JAVA_OPTS} -Dtrue.verbose=true"
#
java -cp ${CP} ${JAVA_OPTS} weatherstation.ws.HomeWeatherStationSimulator
#

