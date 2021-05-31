#!/bin/bash
CP=./build/libs/ImageDB-1.0-all.jar
#
java -cp ${CP} utils.migration.PopulateFromFileSystem $*
