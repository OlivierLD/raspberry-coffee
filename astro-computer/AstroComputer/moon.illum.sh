#!/bin/bash
CP=./build/libs/astro.computer-1.0-all.jar
#
# use --help from the command line for help.
#
java -cp ${CP} celestial.almanac.MoonIllumination $*
