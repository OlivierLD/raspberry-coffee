#!/bin/bash
#
CP=
CP=$CP:./build/libs/RasPISamples-1.0-all.jar
#
JAVA_OPTS=
#
DATA_LOGGERS=
# DATA_LOGGERS="weatherstation.logger.DummyLogger"
DATA_LOGGERS="$DATA_LOGGERS,weatherstation.logger.NMEAOverTCPLogger"
# DATA_LOGGERS="$DATA_LOGGERS,weatherstation.logger.HTTPLogger"
# DATA_LOGGERS="$DATA_LOGGERS,weatherstation.logger.WebSocketLogger"
JAVA_OPTS="$JAVA_OPTS -Ddata.logger=$DATA_LOGGERS"
# JAVA_OPTS="$JAVA_OPTS -Dws.log=true"
# Also -Dws.uri if needed
JAVA_OPTS="$JAVA_OPTS -Dstation.lat=37.7489 -Dstation.lng=-122.5070"
# JAVA_OPTS="$JAVA_OPTS -Dhttp.verbose=true"
#
java -cp $CP $JAVA_OPTS weatherstation.ws.HomeWeatherStationSimulator
