#!/bin/bash
echo ----------------------------
echo Perpetual Nautical Almanac Calculation
echo and pdf generation
echo ----------------------------
#
export SCRIPT_DIR=`dirname $0`
echo moving to ${SCRIPT_DIR}
cd ${SCRIPT_DIR}
#
export HOME=..
#
export CP=${CP}:../../build/libs/RESTNavServer-1.0-all.jar
export CP=${CP}:${HOME}/libs/orai18n-collation.jar
export CP=${CP}:${HOME}/libs/orai18n-mapping.jar
export CP=${CP}:${HOME}/libs/fnd2.zip
export CP=${CP}:${HOME}/libs/xdo-0301.jar
#
echo -en "Generate Data [y]|n ? > "
read genData
if [[ "$genData" = "y" ]] || [[ "$genData" = "" ]]; then
	echo -en "From Year > "
	read from
	echo -en "To Year   > "
	read to
	echo Generating Data...
	java -Xms256m -Xmx2048m -classpath ${CP} implementation.perpetualalmanac.Publisher $from $to ./data.xml
fi
publishData=
echo -en "Publish Data  [y]|n ? >"
read publishData
if [[ "$publishData" = "y" ]] || [[ "$publishData" = "" ]]; then
	echo Processing PDF file
	echo Publishing perpetual almanac
	cp literals_en.xsl literals.xsl
	XSL_STYLESHEET=./perpetual.xsl
	PRM_OPTION="-docconf ./scalable.cfg"
	java -Xms256m -Xmx1024m -classpath ${CP} oracle.apps.xdo.template.FOProcessor ${PRM_OPTION} -xml $1 -xsl $XSL_STYLESHEET -pdf $2
	echo Done calculating, $2 is ready.
fi
#
