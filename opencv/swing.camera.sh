#!/usr/bin/env bash
CP=./build/libs/opencv-1.0-all.jar
CP="$CP:/usr/local/Cellar/opencv/4.1.0_2/share/java/opencv4/opencv-410.jar"
JAVA_LIB_PATH="/usr/local/Cellar/opencv/4.1.0_2/share/java/opencv4" # For Mac
CLASS=oliv.opencv.OpenCVSwingCamera
#
java -cp ${CP} -Djava.library.path=${JAVA_LIB_PATH} ${CLASS}
