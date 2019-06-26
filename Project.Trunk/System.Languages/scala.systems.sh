#!/bin/bash
echo make sure you\'ve run
echo   ../../gradlew clean shadowJar
echo
scala -cp build/libs/System.Languages-1.0.jar systems.SystemUtils
#
