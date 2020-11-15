#!/bin/bash
echo -e "Read an MCP3008 ADC, for orientation (angle) feedback"
echo -e "Use it to calibrate the button driving the wheel (option 1)"
#
CP=./build/libs/Servos-and-Co-1.0-all.jar
#
echo -e "+----------------+"
echo -e "| 1: Calibration |"
echo -e "| 2: For real    |"
echo -e "+------------+---+"
echo -e "| Q: Bye     |"
echo -e "+------------+"
echo -en " You choose > "
read a
#
case "$a" in
  "Q" | "q")
    printf "You're done.\n   Please come back soon!\n"
    ;;
  "1")
		JAVA_OPTS=
		JAVA_OPTS="$JAVA_OPTS -Ddisplay.digit=false"
		JAVA_OPTS="$JAVA_OPTS -Ddebug=false"
		JAVA_OPTS="$JAVA_OPTS -Dcalibration=true"
		#
		echo -e "Usage is $0 --miso:9 --mosi:10 --clk:11 --cs:8 --channel:0"
		echo -e "All arguments are optional. Values above are default values"
		echo -e " >> ! IMPORTANT: For miso, mosi, clk & cs, use BCM pin numbers"
		#
		sudo java -cp ${CP} ${JAVA_OPTS} feedback.one0one.MainMCP3008Sample33Feedback $*
    ;;
  "2")
    echo -e "Enter limit values (-90 and 90 degrees)"
    echo -en "ADC value for -90 degrees > "
    read adcMinus90
    echo -en "ADC value for +90 degrees > "
    read adcPlus90
		JAVA_OPTS=
		JAVA_OPTS="$JAVA_OPTS -Ddisplay.digit=false"
		JAVA_OPTS="$JAVA_OPTS -Ddebug=false"
		JAVA_OPTS="$JAVA_OPTS -Dcalibration=false"
		#
		echo -e "Usage is $0 --miso:9 --mosi:10 --clk:11 --cs:8 --channel:0"
		echo -e " ! IMPORTANT: For miso, mosi, clk & cs, use BCM pin numbers"
		#
		# Comment/uncomment at will
		#
		JAVA_OPTS="$JAVA_OPTS -Dws.uri=ws://localhost:9876/"
		echo -e "Make sure you've started the WS Server, in src/main/node/, 'npm install', and 'node server.js'"
		#
		sudo java -cp ${CP} ${JAVA_OPTS} feedback.one0one.MainMCP3008Sample33Feedback --minus90:${adcMinus90} --plus90:${adcPlus90} $*
    ;;
  *)
    echo -e "What? Unknown command [$a]"
    ;;
esac
#
echo Done.
