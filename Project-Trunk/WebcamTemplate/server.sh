#!/usr/bin/env bash
PROPS=
# PROPS="${PROPS} -Dstatic.docs=/web/"
PROPS="${PROPS} -Dsnap.verbose=true"
java ${PROPS} -jar build/libs/WebcamTemplate-1.0-all.jar
