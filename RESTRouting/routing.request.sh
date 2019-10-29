#!/usr/bin/env bash
#
# Notice the outputType in the request
#
REQUEST_PAYLOAD='{"fromL":37.122,"fromG":-122.5,"toL":-9.75,"toG":-139.1,"startTime":"2017-10-16T07:00:00","gribName":"./GRIB_2017_10_16_07_31_47_PDT.grb","polarFile":"./samples/CheoyLee42.polar-coeff","outputType":"GPX","timeInterval":24,"routingForkWidth":140,"routingStep":10,"limitTWS":-1,"limitTWA":-1,"speedCoeff":0.75,"proximity":25,"avoidLand":false,"verbose":false}'
#
curl -v -H "Content-Type: application/json" -H "Accept: */*" -X POST -d ${REQUEST_PAYLOAD} http://localhost:9876/grib/routing
#
