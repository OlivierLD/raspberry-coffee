#!/usr/bin/env bash
CP=./build/libs/SunFlower.v2-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Ddevice.lat=37.7489 -Ddevice.lng=-122.5070"
# JAVA_OPTS="$JAVA_OPTS -Dazimuth.ratio=1:40"
# JAVA_OPTS="$JAVA_OPTS -Delevation.ratio=18:128"
JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Dmotor.hat.verbose=false"
# JAVA_OPTS="$JAVA_OPTS -Dmin.diff.for.move=0.5"
JAVA_OPTS="$JAVA_OPTS -DdeltaT=69.2201"
#
JAVA_OPTS="$JAVA_OPTS -Dhttp.port=8989"
JAVA_OPTS="$JAVA_OPTS -Dhttp.verbose=false"
#
echo -e "Try 'nohup ./$0 > sf.log &'"
java -cp ${CP} ${JAVA_OPTS} sunflower.httpserver.SunFlowerServer
