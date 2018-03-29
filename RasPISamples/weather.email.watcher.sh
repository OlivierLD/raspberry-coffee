#!/usr/bin/env bash
#
# Script Parameters -send:google -receive:google -verbose -help
#
CP=./build/libs/RasPISamples-1.0-all.jar
#
PROXY=
# PROXY="-Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
java -cp $CP $PROXY weatherstation.email.EmailWatcher $*
#
