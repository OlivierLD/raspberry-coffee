#!/usr/bin/env bash
echo -e "Usage: ${0} xml xsl"
JAR=${HOME}/.m2/repository/oracle/xmlparser/2.0/xmlparser-2.0.jar
java -cp ${JAR} oracle.xml.parser.v2.oraxsl $1 $2


