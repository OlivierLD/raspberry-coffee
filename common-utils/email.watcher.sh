#!/usr/bin/env bash
#
# Script Parameters -send:google -receive:google -verbose -help
#
if [[ $# = 0 ]]
then
  echo ">>> Need parameters, like -send:google -receive:yahoo -verbose -help"
  echo ">>> exiting."
  exit 1
fi
CP=./build/libs/common-utils-1.0-all.jar
#
PROXY=
# PROXY="-Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
OPTIONS=
# OPTIONS="$OPTIONS -Demail.test.only=true"
java -cp ${CP} ${PROXY} ${OPTIONS} email.examples.EmailWatcher $*
#
