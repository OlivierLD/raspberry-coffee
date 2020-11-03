#!/usr/bin/env bash
OPENCV_LOC=/usr/local/share/java/opencv4
CP=
CP="${CP}:${OPENCV_LOC}/opencv-450.jar"
JAVA_LIB_PATH=${OPENCV_LOC}
#
java -Djava.library.path=${OPENCV_LOC} -jar ./build/libs/opencv-1.0-all.jar
