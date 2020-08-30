#!/bin/bash
CP=./build/libs/http-client-samples-1.0-all.jar
JAVA_OPTS="-Drest.url=http://192.168.42.9:8080/lis3mdl/cache"
#
java -cp ${CP} ${JAVA_OPTS} lis3mdl.http.MagnetometerReader
