#!/usr/bin/env bash
#
CP=./build/libs/RESTRouting-1.0-all.jar
#
# java -cp $CP poc.GRIBBulk $*
#
# java -cp $CP poc.GRIBDump
#
JAVA_OPT=
JAVA_OPT="$JAVA_OPT -Dgrib.verbose=false"
JAVA_OPT="$JAVA_OPT -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80"
# Pull GRIB file and dump:
java -cp $CP $JAVA_OPT samples.Sample03 > grib.json
#
