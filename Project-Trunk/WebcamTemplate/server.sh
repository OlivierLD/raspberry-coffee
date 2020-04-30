#!/usr/bin/env bash
PROPS=
# PROPS="${PROPS} -Dstatic.docs=/web/"
# PROPS="${PROPS} -Dsnap.verbose=true"
PROPS="${PROPS} -Dimage.rest.verbose=true"
PROPS="${PROPS} -Dwith.opencv=true"
java ${PROPS} -jar build/libs/WebcamTemplate-1.0-all.jar
