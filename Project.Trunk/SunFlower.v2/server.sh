#!/usr/bin/env bash
#
# Orient the panel for real.
# REST Interface for the data.
# Designed to be started in background.
#
CP=./build/libs/SunFlower.v2-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Ddevice.lat=37.7489 -Ddevice.lng=-122.5070"
JAVA_OPTS="$JAVA_OPTS -Dazimuth.ratio=20:40"  # V3
# JAVA_OPTS="$JAVA_OPTS -Delevation.ratio=18:128"
JAVA_OPTS="$JAVA_OPTS -Dastro.verbose=true"
JAVA_OPTS="$JAVA_OPTS -Dmotor.hat.verbose=true"
JAVA_OPTS="$JAVA_OPTS -Dtoo.long.exception.verbose=false"
# JAVA_OPTS="$JAVA_OPTS -Dmin.diff.for.move=0.5"
JAVA_OPTS="$JAVA_OPTS -DdeltaT=69.2201"
#
JAVA_OPTS="$JAVA_OPTS -Dazimuth.inverted=true"
#
JAVA_OPTS="$JAVA_OPTS -Dminimum.elevation=10"
#
JAVA_OPTS="$JAVA_OPTS -Dhttp.port=8989"
JAVA_OPTS="$JAVA_OPTS -Dhttp.verbose=false"
#
# For Date simulation:
JAVA_OPTS="$JAVA_OPTS -Ddate.simulation=false"
JAVA_OPTS="$JAVA_OPTS -Dstart.date.simulation=2020-03-06T20:00:00"
JAVA_OPTS="$JAVA_OPTS -Dincrement.per.second=600"
#
JAVA_OPTS="$JAVA_OPTS -Dfirst.move.slack=35"
#
JAVA_OPTS="$JAVA_OPTS -Dbetween.astro.loops=10" # Give some time to the motor...
JAVA_OPTS="$JAVA_OPTS -Dno.motor.movement=false"
#
# NMEA Data server
JAVA_OPTS="$JAVA_OPTS -Dping.nmea.server=true"
# JAVA_OPTS="$JAVA_OPTS -Dnmea.server.base.url=http://...:9999"
#
echo -e "Try 'nohup $0 > sf.log &'"
java -cp ${CP} ${JAVA_OPTS} sunflower.httpserver.SunFlowerServer
