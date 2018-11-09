#!/usr/bin/env bash
#
CP=./build/libs/RESTRouting-1.0-all.jar
#
# Example:
# ./run.routing.sh  -fromL 37.122 -fromG -122.5 -toL -9.75 -toG -139.10 -startTime "2017-10-16T07:00:00" -grib "./GRIB_2017_10_16_07_31_47_PDT.grb" -polars "./samples/CheoyLee42.polar-coeff" -output "JSON" -speedCoeff 0.75 -verbose true
#
java -cp $CP gribprocessing.utils.BlindRouting $*
#
