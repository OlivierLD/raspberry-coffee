#!/bin/bash
echo ----------------------------
echo Design publisher
echo ----------------------------
#
export SCRIPT_DIR=$(dirname $0)
echo -e "Moving to ${SCRIPT_DIR}"
cd ${SCRIPT_DIR}
echo -e "Working from $(pwd -P)"
#
export HOME=../../../RESTNavServer/launchers
#
export CP=
# export CP=${CP}:${HOME}/../build/libs/RESTNavServer-1.0-all.jar  # For the XML Parser
export CP=${CP}:../build/libs/BoatDesign-1.0-all.jar  # For the XML Parser
export CP=${CP}:${HOME}/libs/orai18n-collation.jar
export CP=${CP}:${HOME}/libs/orai18n-mapping.jar
export CP=${CP}:${HOME}/libs/fnd2.zip
export CP=${CP}:${HOME}/libs/xdo-0301.jar
#
XSL_STYLESHEET=
PRM_OPTION="-docconf ./config.cfg"
LOOP=true
while [ "$LOOP" == "true" ]; do
	clear
	echo -e "+-------------------------------+"
	echo -e "| Publication - Tables 900      |"
	echo -e "+-------------------------------+"
	echo -e "| 0. FOP Processor help         |"
	echo -e "| 1. First test (pdf)           |"
	echo -e "+-------------------------------+"
	echo -e "| 2. HTML test (demo)           |"
	echo -e "+-------------------------------+"
	echo -e "| Q. Quit                       |"
	echo -e "+-------------------------------+"
	echo -en "You choose > "
	read resp
	case "$resp" in
    "Q" | "q")
      LOOP=false
      printf "You're done.\n   Please come back soon!\n"
      ;;
    "0")
      # Doc at https://docs.oracle.com/cd/B24289_01/current/acrobat/115xdoug.pdf
			COMMAND="java -Xms256m -Xmx1536m -classpath ${CP} oracle.apps.xdo.template.FOProcessor -h"
			${COMMAND}
			echo "Hit Return"
			read a
      ;;
    "1")
			echo Publishing, please be patient...
			#
			echo -e "Create the data.xml here..."
			#
			DATA=data.2.xml
			XSL=./data-fo.xsl
			OUTPUT=data.pdf
			COMMAND="java -Xms256m -Xmx1536m -classpath ${CP} oracle.apps.xdo.template.FOProcessor ${PRM_OPTION} -xml ${DATA} -xsl ${XSL} -pdf ${OUTPUT}"
			echo Running from $PWD: ${COMMAND}
			${COMMAND}
			echo Done transforming, document is ready.
			echo "Hit Return"
			read a
      ;;
    "2")
#			java -classpath ${CP} tables.Dieumegard > dieumegard.xml
			#
      java -classpath ${CP} oracle.xml.parser.v2.oraxsl -s html.xsl -l dieumegard.xml -o . -r html
      #
      open dieumegard.xml.html
      #
			echo "Hit Return"
			read a
      ;;
     *)
      echo -e "Unknown command [${resp}]"
			echo "Hit Return"
			read a
      ;;
	esac
done
#
