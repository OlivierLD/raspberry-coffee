#!/usr/bin/env bash
#
# Orient the panel for real.
#
# REST Interface for the data.
# Designed to be started in background.
#
# Note: If the computer if not connected to the Internet, you need to get the date from the GPS.
# (see -Ddate.from.gps=true|false)
#
CP=./build/libs/SunFlower-v2-1.0-all.jar
JAVA_OPTS=

JAVA_OPTS="${JAVA_OPTS} -Dsun.flower.verbose=false"  # REST snitch
# JAVA_OPTS="${JAVA_OPTS} -Ddevice.lat=37.7489 -Ddevice.lng=-122.5070"  # SF
JAVA_OPTS="${JAVA_OPTS} -Ddevice.lat=47.677667 -Ddevice.lng=-3.135667"  # Belz
JAVA_OPTS="${JAVA_OPTS} -Dazimuth.ratio=16:76"  # For V5
# JAVA_OPTS="${JAVA_OPTS} -Dazimuth.ratio=20:40"  # For V3
JAVA_OPTS="${JAVA_OPTS} -Delevation.ratio=18:128"
JAVA_OPTS="${JAVA_OPTS} -Dastro.verbose=false"
JAVA_OPTS="${JAVA_OPTS} -Dmoves.verbose=false"
JAVA_OPTS="${JAVA_OPTS} -Dmotor.hat.verbose=false"
JAVA_OPTS="${JAVA_OPTS} -Dtoo.long.exception.verbose=false"
JAVA_OPTS="${JAVA_OPTS} -Dspecial.debug.verbose=false"
# JAVA_OPTS="${JAVA_OPTS} -Dmin.diff.for.move=0.5"
JAVA_OPTS="${JAVA_OPTS} -DdeltaT=AUTO"
#
JAVA_OPTS="${JAVA_OPTS} -Dazimuth.inverted=false"  # For V5
# JAVA_OPTS="${JAVA_OPTS} -Dazimuth.inverted=true"
#
JAVA_OPTS="${JAVA_OPTS} -Dminimum.elevation=10"
#
JAVA_OPTS="${JAVA_OPTS} -Duse.step.accumulation=false"  # true
#
PORT=8989
JAVA_OPTS="${JAVA_OPTS} -Dhttp.port=${PORT}"
JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose=false"
#
# For Date simulation:
JAVA_OPTS="${JAVA_OPTS} -Ddate.simulation=false"
JAVA_OPTS="${JAVA_OPTS} -Dstart.date.simulation=2020-03-06T20:00:00"  # UTC
JAVA_OPTS="${JAVA_OPTS} -Dincrement.per.second=600"                   # In seconds
#JAVA_OPTS="${JAVA_OPTS} -Ddate.simulation=true"
#JAVA_OPTS="${JAVA_OPTS} -Dstart.date.simulation=2020-09-08T13:45:00"  # UTC
#JAVA_OPTS="${JAVA_OPTS} -Dincrement.per.second=1"                     # In seconds
#
JAVA_OPTS="${JAVA_OPTS} -Dfirst.move.slack=35"
#
JAVA_OPTS="${JAVA_OPTS} -Dbetween.astro.loops=10" # Give some time to the motor...
#JAVA_OPTS="${JAVA_OPTS} -Dbetween.astro.loops=1"
JAVA_OPTS="${JAVA_OPTS} -Dno.motor.movement=false" # Set to true NOT to use the motors
#
# NMEA Data server (NMEA-multiplexer), position and heading (See in FeatureRequestManager)
JAVA_OPTS="${JAVA_OPTS} -Dping.nmea.server=false"  # Fetch data from multiplexer
JAVA_OPTS="${JAVA_OPTS} -Dnmea.server.base.url=http://192.168.42.30:9991"  # Used as base for GET /mux/cache. Can be on any machine.
#
JAVA_OPTS="${JAVA_OPTS} -Dwith.ssd1306=true" # OLED! Default 128x32
JAVA_OPTS="${JAVA_OPTS} -Dssd1306.verbose=false"
#
JAVA_OPTS="${JAVA_OPTS} -Djava.util.logging.config.file=logging.properties"
#
# Add a GPS on a Serial Port (to get RMC Date and Position)
# Make sure ping.nmea.server=false
# May require a link sudo ln -s /dev/ttyACM0 /dev/ttyS80. See gps.serial.port below.
#           and sudo apt-install librxtx-java
JAVA_OPTS="${JAVA_OPTS} -Ddate.from.gps=false"   # if false, next ones are ignored.
JAVA_OPTS="${JAVA_OPTS} -Dgps.verbose=false"
JAVA_OPTS="${JAVA_OPTS} -Dgps.serial.baud.rate=4800"
#
# uname -s: Linux
OS=$(uname -s)
case "${OS}" in
  "Darwin")
    JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/Library/Java/Extensions"  # for Mac
    JAVA_OPTS="${JAVA_OPTS} -Dgps.serial.port=/dev/tty.usbmodem141101"
    ;;
  "Linux" | *)
    JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/usr/lib/jni"              # RPi
    JAVA_OPTS="${JAVA_OPTS} -Dgps.serial.port=/dev/ttyS80"
    ;;
esac
#
CP=${CP}:/usr/share/java/RXTXcomm.jar                                # For Raspberry Pi
#
REMOTE_DEBUG_FLAGS=
# JDK 9 and up
# REMOTE_DEBUG_FLAGS="${REMOTE_DEBUG_FLAGS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
# JDK 5-8
# REMOTE_DEBUG_FLAGS="${REMOTE_DEBUG_FLAGS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
JAVA_OPTS="${JAVA_OPTS} ${REMOTE_DEBUG_FLAGS}"
MY_IP=$(hostname -I | awk '{ print $1 }')
MY_IP=$(echo ${MY_IP})  # Trim the blanks
echo -e "Try curl -X GET http://${MY_IP}:${PORT}/sf/status"
echo -e "Or browse http://${MY_IP}:${PORT}/web/index.html"
echo -e "       or http://${MY_IP}:${PORT}/zip/index.html"
#
echo -e "Also try 'nohup ${0} > sf.log &'"
java -cp ${CP} ${JAVA_OPTS} sunflower.main.SunFlowerServer
