#!/usr/bin/env bash
#
# Actual Program, orient the panel for real.
# ANSI Console UI.
#
CP=./build/libs/SunFlower-v2-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Ddevice.lat=37.7489 -Ddevice.lng=-122.5070"
JAVA_OPTS="$JAVA_OPTS -Dazimuth.ratio=16:76"  # For V5
# JAVA_OPTS="$JAVA_OPTS -Dazimuth.ratio=20:40"  # For V3
JAVA_OPTS="$JAVA_OPTS -Delevation.ratio=18:128"
JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Dmotor.hat.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Dsun.flower.verbose=false"
JAVA_OPTS="$JAVA_OPTS -Dansi.boxes=true"
JAVA_OPTS="$JAVA_OPTS -Dazimuth.inverted=false"  # For V5
# JAVA_OPTS="$JAVA_OPTS -Dazimuth.inverted=true"
#
# For Date simulation, uncomment the lines below
#JAVA_OPTS="$JAVA_OPTS -Ddate.simulation=true"
## JAVA_OPTS="$JAVA_OPTS -Dstart.date.simulation=2020-03-06T20:00:00"
#JAVA_OPTS="$JAVA_OPTS -Dstart.date.simulation=2020-06-08T15:00:00"
#JAVA_OPTS="$JAVA_OPTS -Dincrement.per.second=600"
#JAVA_OPTS="$JAVA_OPTS -Dbetween.astro.loops=10"
#
JAVA_OPTS="$JAVA_OPTS -Duse.step.accumulation=true"
#
JAVA_OPTS="$JAVA_OPTS -Dwith.ssd1306=true"
#
# echo -e "Using JAVA_OPTS: ${JAVA_OPTS}"
#
java -cp ${CP} ${JAVA_OPTS} sunflower.main.ConsoleMain 2>error.txt
#
# java -cp ${CP} ${JAVA_OPTS} sunflower.utils.EscapeSeq
