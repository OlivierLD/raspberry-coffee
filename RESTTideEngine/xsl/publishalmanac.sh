#!/bin/bash
echo ----------------------------
echo Almanac publisher
echo Usage is: ${0} lang withStars xmlData pdf
echo lang: EN\|FR
echo withStars: true\|false
echo xmlData: computed XML data file
echo pdf: name of the final document
echo example: ${0} EN true ../../data.2017.xml almanac.2017.pdf
echo ----------------------------
#
export SCRIPT_DIR=`dirname ${0}`
echo moving to ${SCRIPT_DIR}
cd ${SCRIPT_DIR}
#
export HOME=..
#
export CP=${CP}:../build/libs/RESTTideEngine-1.0-all.jar
export CP=${CP}:${HOME}/libs/orai18n-collation.jar
export CP=${CP}:${HOME}/libs/orai18n-mapping.jar
export CP=${CP}:${HOME}/libs/fnd2.zip
export CP=${CP}:${HOME}/libs/xdo-0301.jar
#
XSL_STYLESHEET=./data2fop_2pages.xsl
LANG=S1
WITH_STARS=$2
PRM_OPTION=
if [[ $LANG = "FR" ]]; then
  echo On parle francais
  PRM_OPTION="-docconf ./lang_fr.cfg"
  if [[ $WITH_STARS = "false" ]]; then
    PRM_OPTION="-docconf ./lang_fr_ns.cfg"
  fi
  cp literals_fr.xsl literals.xsl
else
  echo Will speak English
  cp literals_en.xsl literals.xsl
  PRM_OPTION="-docconf ./lang_en.cfg"
  if [[ $WITH_STARS = "false" ]]; then
    PRM_OPTION="-docconf ./lang_en_ns.cfg"
  fi
fi
echo Publishing, be patient.
#
# Note on -Xmx:
# A 1 year almanac with stars is about 11Mb big, and 1024 seems to be a bit too tight.
#
java -Xms256m -Xmx1536m -classpath ${CP} oracle.apps.xdo.template.FOProcessor ${PRM_OPTION} -xml $3 -xsl $XSL_STYLESHEET -pdf $4
echo Done transforming, document $4 is ready.
#
