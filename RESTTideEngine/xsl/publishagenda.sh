#!/bin/bash
#
# This is a bonus.
# It is actually the Tide Almanac, without the tide data.
#
echo ----------------------------
echo Agenda publisher
echo ----------------------------
#
export SCRIPT_DIR=`dirname $0`
echo moving to ${SCRIPT_DIR}
cd ${SCRIPT_DIR}
#
export RADICAL=$1
echo Transforming ${RADICAL}.xml into ${RADICAL}.pdf
#
export HOME=..
#
export CP=$CP:../build/libs/RESTTideEngine-1.0-all.jar
export CP=${CP}:${HOME}/libs/orai18n-collation.jar
export CP=${CP}:${HOME}/libs/orai18n-mapping.jar
export CP=${CP}:${HOME}/libs/fnd2.zip
export CP=${CP}:${HOME}/libs/xdo-0301.jar
#
export XSL_STYLESHEET=./agenda2fop.xsl
echo Publishing
java -Xms256m -Xmx1024m -classpath ${CP} oracle.apps.xdo.template.FOProcessor -xml ${RADICAL}.xml -xsl ${XSL_STYLESHEET} -pdf ${RADICAL}.pdf
echo Done transforming, document ${RADICAL}.pdf is ready.
# open ${RADICAL}.pdf
# exit
