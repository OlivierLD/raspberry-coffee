#!/usr/bin/env bash
PROPS=
# PROPS="${PROPS} -Dstatic.docs=/web/"
# PROPS="${PROPS} -Dsnap.verbose=true"
PROPS="${PROPS} -Dimage.rest.verbose=true"
PROPS="${PROPS} -Dwith.opencv=true"
#
OPENCV_LOC=/usr/local/share/java/opencv4
#
java ${PROPS} -Djava.library.path=${OPENCV_LOC} -jar build/libs/WebcamTemplate-1.0-all.jar
