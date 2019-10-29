#!/usr/bin/env bash
#
# Standalone test (no server)
#
CP=./build/libs/RESTRouting-1.0-all.jar
#
# Example:
# ./run.routing.sh  --from-lat 37.122 --from-lng -122.5 --to-lat -9.75 --to-lng -139.10 --start-time "2017-10-16T07:00:00" --grib-file "./GRIB_2017_10_16_07_31_47_PDT.grb" --polar-file "./samples/CheoyLee42.polar-coeff" --output-type "JSON" --speed-coeff 0.75 --verbose true
#
java -cp ${CP} gribprocessing.utils.BlindRouting $*
#
