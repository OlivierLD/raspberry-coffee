#!/bin/bash
#
# POST the snap.jpg image to AdafruitIO, feed 'picture'

# This script refers to 'all' archives, like Adafruit.IO.REST-1.0-all.jar, containing
# all dependencies like json.jar.
# Those archives are built from the root like that:
#  raspberry-pi4j-samples $ ./gradlew shadowJar
#
CP=./build/libs/Adafruit.IO.REST-1.0-all.jar
#
if [ "$1" = "" ]
then
  echo Need your Adafruit-IO key as parameter.
  echo Aborting.
  exit 1
fi
#
# Optional: pull the image from where it has been taken
scp pi@192.168.42.2:~/snap.jpg snap.jpg
#
JAVA_OPTIONS=
# JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# sudo java -Dkey=$1 -cp $CP adafruit.io.sample.POSTSnapshot
java -Dkey=$1 -cp $CP $JAVA_OPTIONS adafruit.io.sample.POSTSnapshot
