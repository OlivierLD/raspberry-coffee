#!/usr/bin/env bash
#
# WARNING: Works for both STH10 and MCP3008. Comment/uncomment the required lines below
#
CP=build/libs/PlantWateringSystem-1.0-all.jar
#
echo "Usage is $0 debug remote-debug [verbose|ansi] wait"
echo "   Use 'remote-debug' to remote-debug from another machine."
echo "   Use 'verbose' for a regular look on what's going on, oe 'ansi' for an ansi console."
echo "   Use 'debug' for a close look on what's going on."
echo "   Use 'wait' to wait 10 sec before actually starting."
#
echo `date`
#
VERBOSE=false
ANSI=false
DEBUG=false
REMOTE_DEBUG=false
WAIT=false
#
SUDO=
DARWIN=$(uname -a | grep Darwin)
#
if [ "$DARWIN" != "" ]
then
	echo Running on Mac
  JAVA_OPTS="$JAVA_OPTS -Djava.library.path=/Library/Java/Extensions"  # for Mac
else
	echo Assuming Linux/Raspberry Pi
  JAVA_OPTS="$JAVA_OPTS -Djava.library.path=/usr/lib/jni"              # RPi
  SUDO="sudo "
fi
#
for ARG in "$@"
do
	echo -e "Managing prm $ARG"
	if [ "$ARG" == "verbose" ]
	then
	  VERBOSE=true
	elif [ "$ARG" == "ansi" ]
	then
	  ANSI=true
	elif [ "$ARG" == "wait" ]
	then
	  WAIT=true
	elif [ "$ARG" == "debug" ]
	then
	  DEBUG=true
	elif [ "$ARG" == "remote-debug" ]
	then
	  REMOTE_DEBUG=true
	fi
done
#
JAVA_OPTIONS="-Dsth.debug=$DEBUG"
JAVA_OPTIONS="$JAVA_OPTIONS -Dmcp3008.debug=$DEBUG"
#
if [ "$REMOTE_DEBUG" == "true" ]
then
  # For remote debugging:
  JAVA_OPTIONS="$JAVA_OPTIONS -client -agentlib:jdwp=transport=dt_socket,server=y,address=4000"
fi
# For remote JVM Monitoring
# JAVA_OPTIONS="$JAVA_OPTIONS -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=raspberrypi-boat"
#
# Use program argument --help for help.
#
if [ "$VERBOSE" == "true" ]
then
  # java $JAVA_OPTIONS -cp $CP main.STH10 --help
  java $JAVA_OPTIONS -cp $CP main.MCP3008 --help
  #
  echo -n "Hit return... "
  read a
fi
#
# verbose: ANSI, STDOUT, NONE
if [ "$VERBOSE" == "true" ]
then
  USER_PRM="--verbose:STDOUT"
elif [ "$ANSI" == "true" ]
then
  USER_PRM="--verbose:ANSI"
else
  USER_PRM="--verbose:NONE"
fi
#
# Start watering below 50%, for 10 seconds. Resume watching after 120 seconds.
#
HUMIDITY_THRESHOLD=75
USER_PRM="$USER_PRM --water-below:${HUMIDITY_THRESHOLD} --water-during:10 --resume-after:120"
#
# REST and Web Server
#
USER_PRM="$USER_PRM --with-rest-server:true --http-port:8088"
#
# No space in the logger list!
# Warning: the FileLogger writes on the disk, clean it from time to time...
#
LOGGERS="loggers.iot.AdafruitIOClient"
IOT_FEED_NAME="humidity"
LOGGERS="$LOGGERS,loggers.text.FileLogger"
USER_PRM="$USER_PRM --loggers:$LOGGERS"
#
now=`date +%Y-%m-%d.%H:%M:%S`
LOG_FILE_NAME=${now}_log.log
echo -e "Logging data in ${LOG_FILE_NAME}"
JAVA_OPTIONS="$JAVA_OPTIONS -Dlogger.file.name=${LOG_FILE_NAME}"
#
JAVA_OPTIONS="$JAVA_OPTIONS -Daio.key=54c2767878ca793f2e3cae1c45d62aa7ae9f8056"
JAVA_OPTIONS="$JAVA_OPTIONS -Daio.verbose=false"
JAVA_OPTIONS="$JAVA_OPTIONS -Diot.hum.feed=$IOT_FEED_NAME"
#
# USER_PRM="$USER_PRM --simulate-sensor-values:true" # Values can be entered from a REST service, POST /pws/sth10-data
#
# JAVA_OPTIONS="$JAVA_OPTIONS -Drandom.simulator=true"
# USER_PRM="$USER_PRM --water-below:50 --water-during:10 --resume-after:120"
#
JAVA_OPTIONS="$JAVA_OPTIONS -Dgpio.verbose=true -Dansi.boxes=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dvalve.test=true"
#
# Will send email if water tank runs empty
# Refers to (and requires) email.properties
JAVA_OPTIONS="$JAVA_OPTIONS -Demail.provider=google"
JAVA_OPTIONS="$JAVA_OPTIONS -Demail.verbose=true"
#
#
# java $JAVA_OPTIONS -cp $CP main.STH10 $USER_PRM
#
# Use for example --verbose:STDOUT --miso-pin:23 --mosi-pin:24 --clk-pin:18 --cs-pin:25 --adc-channel-pin::0 --relay-pin:17
# Depends on your wiring
#
PIN_MAPPING="--miso-pin:23 --mosi-pin:24 --clk-pin:18 --cs-pin:25 --adc-channel-pin:0 --relay-pin:17"
LOGGING_FLAG=
LOGGING_FLAG="-Djava.util.logging.config.file=./logging.properties"
#
# JAVA_OPTIONS="$JAVA_OPTIONS -Dslowdown.for.debug=true"
#
# COMMAND="${SUDO}java $JAVA_OPTIONS -cp $CP main.STH10 $USER_PRM"
COMMAND="${SUDO}java $JAVA_OPTIONS -cp $CP $LOGGING_FLAG main.MCP3008 $USER_PRM $PIN_MAPPING"
if [ "$DEBUG" == "true" ]
then
	 echo "COMMAND is: $COMMAND"
	 echo -n "Hit return... "
	 read a
fi
if [ "$WAIT" == "true" ]
then
	echo Waiting 10 sec
  sleep 10
fi
#
echo -e "Running $COMMAND"
$COMMAND
#
