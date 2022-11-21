#!/usr/bin/env bash
echo Usage:
echo  ${0} [AIO Key]
echo like ${0} abc8736hgfd78638620ngs
if [[ $# -eq 1 ]]; then
  CP=./build/libs/Monitor.Battery-1.0-all.jar
  JAVA_OPTS="-Daio.key=$1"
  # if proxy
  JAVA_OPTS="${JAVA_OPTS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
  java ${JAVA_OPTS} -cp ${CP} sample.rest.PostSwitch
else
  echo Please provide the expected parameter.
fi
#
