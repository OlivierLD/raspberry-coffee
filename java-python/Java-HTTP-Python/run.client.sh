#!/bin/bash
JAR=build/libs/Java-HTTP-Python-1.0-all.jar
OPTIONS=
# OPTIONS="-Dverbose=true -Drest.url=http://192.168.1.106:8080/lis3mdl/cache"
java ${OPTIONS} -jar ${JAR}
