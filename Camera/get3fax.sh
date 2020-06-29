#!/bin/bash
CP=./build/libs/Camera-1.0-all.jar
#
JAVA_OPTIONS=
# JAVA_OPTIONS="$JAVA_OPTION -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80"
#
java -cp $CP $JAVA_OPTIONS image.util.samples.SeveralFaxes
