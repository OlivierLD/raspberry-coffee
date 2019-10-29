#!/usr/bin/env bash
#
CP=./build/libs/RESTRouting-1.0-all.jar
#
# java -cp $CP poc.GRIBBulk
#
# java -cp $CP poc.GRIBDump > json.txt
#
JAVA_OPT=
JAVA_OPT="$JAVA_OPT -Dhttp.port=8080"
# JAVA_OPT="$JAVA_OPT -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80"
# For remote debugging:
# JAVA_OPT="$JAVA_OPT -client -agentlib:jdwp=transport=dt_socket,server=y,address=4000"
#
java -cp ${CP} ${JAVA_OPT} gribprocessing.GRIBServer
#
