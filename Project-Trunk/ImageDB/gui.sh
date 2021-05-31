#!/bin/bash
CP=./build/libs/ImageDB-1.0-all.jar
#
DB_LOCATION="sql/images.db"
java -cp ${CP} -Ddb.location=${DB_LOCATION} dnd.gui.MainGUI $*
