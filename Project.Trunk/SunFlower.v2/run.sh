#!/usr/bin/env bash
#
echo -e "Interactive tests"
#
CP=./build/libs/SunFlower.v2-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Ddevice.lat=37.7489 -Ddevice.lng=-122.5070"
# JAVA_OPTS="$JAVA_OPTS -Dazimuth.ratio=1:40"
# JAVA_OPTS="$JAVA_OPTS -Delevation.ratio=18:128"
JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Dmotor.hat.verbose=false"
#
JAVA_OPTS="$JAVA_OPTS -Dcalibration=true"
JAVA_OPTS="$JAVA_OPTS -Dsun.flower.verbose=true"
#
# Default is 30, can be increased if not MICROSTEP
JAVA_OPTS="$JAVA_OPTS -Drpm=30"
#
JAVA_OPTS="$JAVA_OPTS -Dstepper.style=MICROSTEP"
# JAVA_OPTS="$JAVA_OPTS -Dstepper.style=SINGLE"
# JAVA_OPTS="$JAVA_OPTS -Dstepper.style=DOUBLE"
# JAVA_OPTS="$JAVA_OPTS -Dstepper.style=INTERLEAVE"
#
java -cp ${CP} ${JAVA_OPTS} sunflower.SunFlowerDriver
