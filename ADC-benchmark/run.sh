#!/bin/bash
echo Read an ADC for 3.3 Volt estimation
#
echo -e "+------------+"
echo -e "| 1: MCP3008 |"
echo -e "| 2: ADS1015 |"
echo -e "| 3: ADS1115 |"
echo -e "+------------+"
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
		JAVA_OPTS="${JAVA_OPTS} -Ddisplay.digit=false"
		JAVA_OPTS="${JAVA_OPTS} -Ddebug=false"
		CP=./build/libs/ADC-benchmark-1.0-all.jar
		#
		echo -e "Usage is $0 --miso:9 --mosi:10 --clk:11 --cs:8 --channel:0"
		echo -e " For miso, mosi, clk & cs, use BCM pin numbers"
		#
		sudo java -cp ${CP} ${JAVA_OPTS} adcbenchmark.mcp3008.MainMCP3008Sample33 $*
    ;;
  "2")
		JAVA_OPTS=
		CP=./build/libs/ADC-benchmark-1.0-all.jar
		#
		sudo java -cp ${CP} ${JAVA_OPTS} adcbenchmark.ads1015.MainADS1015Sample33 $*
    ;;
   "3")
		JAVA_OPTS=
		CP=./build/libs/ADC-benchmark-1.0-all.jar
		#
		sudo java -cp ${CP} ${JAVA_OPTS} adcbenchmark.ads1115.MainADS1115Sample33 $*
    ;;
  *)
    echo -e "What? Unknown command [$a]"
    ;;
esac
#
echo Done.
