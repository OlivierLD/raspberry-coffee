#!/usr/bin/env bash
CP=./build/libs/SunFlower.v2-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Ddevice.lat=37.7489 -Ddevice.lng=-122.5070"
# JAVA_OPTS="$JAVA_OPTS -Dazimuth.ratio=1:40"
# JAVA_OPTS="$JAVA_OPTS -Delevation.ratio=18:128"
JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Dmotor.hat.verbose=false"
#
java -cp ${CP} ${JAVA_OPTS} sunflower.SunFlowerDriver
