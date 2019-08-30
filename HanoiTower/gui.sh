#!/usr/bin/env bash
#
JAVA_OPTS=
# JAVA_OPTS="$JAVA_OPTS -Dwith.perspective=false"
# JAVA_OPTS="$JAVA_OPTS -Dwith.smooth=false"
# JAVA_OPTS="$JAVA_OPTS -Dwith.texture=false"
# JAVA_OPTS="$JAVA_OPTS -Dwith.gradient=false"
JAVA_OPTS="$JAVA_OPTS -Dwith.transparency=false"
#
java $JAVA_OPTS -cp ./build/libs/HanoiTower-1.0-all.jar hanoitower.gui.HanoiSolver
