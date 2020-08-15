#!/bin/bash
CP=./build/libs/I2C-SPI-1.0-all.jar
#
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp://192.168.42.9:8080/lis3mdl/cache"
#
java -cp ${CP} ${JAVA_OPTIONS} frompython.http.MagnetometerReader

