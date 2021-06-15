#!/bin/bash
CP=./build/libs/ImageDB-1.0-all.jar
#
DB_LOCATION="sql/images.db"
if [[ $# -gt 0 ]]
then
  DB_LOCATION=$1
fi
echo -e "Using DB ${DB_LOCATION}"
java -cp ${CP} -Ddb.location=${DB_LOCATION} dnd.gui.MainGUI $*
