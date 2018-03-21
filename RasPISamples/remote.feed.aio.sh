#!/usr/bin/env bash
#
CP=./build/libs/RasPISamples-1.0-all.jar
#
java -cp $CP weatherstation.ImageEncoder web/snap-test.jpg > web/encoded.txt
#
PROXY=
PROXY="-Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
#
java -cp $CP $PROXY -Dkey=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx weatherstation.POSTImage web/encoded.txt
