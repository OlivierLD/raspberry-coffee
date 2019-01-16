#!/bin/bash
echo ----------------------------
echo T900 publisher
echo ----------------------------
#
export SCRIPT_DIR=`dirname $0`
echo moving to ${SCRIPT_DIR}
cd ${SCRIPT_DIR}
#
export HOME=..
#
export CP=$CP:../../build/libs/RESTNavServer-1.0-all.jar
export CP=${CP}:${HOME}/libs/orai18n-collation.jar
export CP=${CP}:${HOME}/libs/orai18n-mapping.jar
export CP=${CP}:${HOME}/libs/fnd2.zip
export CP=${CP}:${HOME}/libs/xdo-0301.jar
#
XSL_STYLESHEET=
PRM_OPTION="-docconf ./config.cfg"
LOOP=true
while [ "$LOOP" == "true" ]
do
	clear
	echo -e "+-------------------------+"
	echo -e "| Publication - Table 900 |"
	echo -e "+-------------------------+"
	echo -e "| 1. Tables de Dieumegard |"
	echo -e "| 2. Tables de Bataille   |"
	echo -e "+-------------------------+"
	echo -e "| Q. Quit                 |"
	echo -e "+-------------------------+"
	echo -en "You choose > "
	read resp
	case "$resp" in
    "Q" | "q")
      LOOP=false
      printf "You're done.\n   Please come back soon!\n"
      ;;
    "1")
			echo Publishing, please be patient...
			#
			java -classpath $CP tables.Dieumegard > dieumegard.xml
			#
			COMMAND="java -Xms256m -Xmx1536m -classpath ${CP} oracle.apps.xdo.template.FOProcessor $PRM_OPTION -xml dieumegard.xml -xsl ./dieumegard-fo.xsl -pdf dieumegard.pdf"
			echo Running from $PWD: $COMMAND
			$COMMAND
			echo Done transforming, document is ready.
			echo "Hit Return"
			read a
      ;;
    "2")
			echo Publishing, please be patient...
			#
			java -classpath $CP tables.Bataille > bataille.xml
			#
			COMMAND="java -Xms256m -Xmx1536m -classpath ${CP} oracle.apps.xdo.template.FOProcessor $PRM_OPTION -xml bataille.xml -xsl ./bataille-fo.xsl -pdf bataille.pdf"
			echo Running from $PWD: $COMMAND
			$COMMAND
			echo Done transforming, document is ready.
			echo "Hit Return"
			read a
      ;;
	esac


done

#
