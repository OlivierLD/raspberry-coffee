#!/usr/bin/env bash
#
if [[ $# != 2 ]]; then
  echo -e "Usage is $0 [log.file.name] [csv.file.name]"
  echo -e "example: $0 sample.data/2010-11.03.Taiohae.nmea today.csv"
  exit 1
fi
#
BREAK_AT=RMC
# DATA=RMC,HDG,HDM,VHW,MWV,MTW,DBT,VWR
DATA=RMC,HDM,VHW,DBT,VWR,MTW
#
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
JAVA_OPTIONS=
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=true"
java ${JAVA_OPTIONS} -cp ${CP} util.NMEAtoCSV "--in:$1" "--out:$2" "--data:${DATA}" "--break-at:${BREAK_AT}"
#
