#!/bin/bash
CP=build/libs/RESTImageProcessor-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="${JAVA_OPTS} -Dhttp.port=9876"
JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose=false"
JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose.dump=true"
#
java -cp ${CP} ${JAVA_OPTS} imageprocessing.ImgServer
