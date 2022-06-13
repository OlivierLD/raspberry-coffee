#!/usr/bin/env bash
#
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
JAVA_OPTS=
# JAVA_OPTS="${JAVA_OPTS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80"
# java -cp ${CP} ${JAVA_OPTS} nmea.consumers.client.AISClientV2
java -cp ${CP} ${JAVA_OPTS} nmea.consumers.client.AISClient
