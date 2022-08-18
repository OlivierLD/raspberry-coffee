#!/bin/bash
# CP=./build/libs/ImageDB-1.0.jar
CP=./build/libs/ImageDB-1.0-all.jar
#
DB_LOCATION="sql/images.db"
if [[ $# -gt 0 ]]; then
  DB_LOCATION=$1
fi
echo -e "Using DB ${DB_LOCATION}"
MAIN_CLASS=dnd.gui.splash.Splasher
# MAIN_CLASS=dnd.gui.MainGUI
# java -cp ${CP} -Ddb.location=${DB_LOCATION} ${MAIN_CLASS} $*
java -cp ${CP} -Ddb.location=${DB_LOCATION} ${MAIN_CLASS} --db-location:$1
