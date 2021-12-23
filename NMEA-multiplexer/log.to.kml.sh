#!/usr/bin/env bash
#
if [[ $# != 3 ]]
then
  echo -e "Usage is $0 [log.file.name] [title] [sub-title]"
  echo -e "example: $0 sample.data/estero.drake.2018-09-29.nmea \"Kayak Drake Estero\" \"28-Sep-2018\""
  exit 1
fi
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
JAVA_OPTIONS=
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Drmc.date.offset=7168"
# sudo java ${JAVA_OPTIONS} $LOGGING_FLAG $JFR_FLAGS $REMOTE_DEBUG_FLAGS -cp ${CP} nmea.mux.GenericNMEAMultiplexer
java ${JAVA_OPTIONS} -cp ${CP} util.NMEAtoKML "$1" "--title:$2" "--sub-title:$3"
#
