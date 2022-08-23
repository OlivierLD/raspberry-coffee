#!/bin/bash
JAR_FILE_NAME=./build/libs/small-server-extended-1.0-all.jar
if [[ -f ${JAR_FILE_NAME} ]]; then
  jar -xf ${JAR_FILE_NAME} META-INF/MANIFEST.MF
  cat META-INF/MANIFEST.MF
  rm -rf META-INF
else
  echo -e "Jar ${JAR_FILE_NAME} not found..."
fi
