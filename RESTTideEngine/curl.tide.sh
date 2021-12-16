#!/bin/bash
#
# Example:
# Query the water height for Port-Tudy for the current time.
#
# Use it with jq if VERBOSE == false (see below).
# ./curl.tide.sh | jq
#   or
# ./curl.tide.sh | jq '.heights | objects[] | .wh'
#
# jq cheatsheet https://lzone.de/cheat-sheet/jq
#
VERBOSE=false
if [[ "$1" == "--verbose" ]]
then
  VERBOSE=true
fi
#
HTTP_PORT=8080
TIDE_STATION="Port-Tudy, France"
# Escaping
TIDE_STATION=$(echo ${TIDE_STATION/,/%2C})
TIDE_STATION=$(echo ${TIDE_STATION/ /%20})
# TIDE_STATION="Port-Tudy%2C%20France"  # Escaped for "Port-Tudy, France"
# Dates, Duration format.
NOW=$(date +"%Y-%m-%dT%T")
#
# REQUEST="http://localhost:${HTTP_PORT}/tide/tide-stations/${TIDE_STATION}/wh?from=${NOW}&to=2021-12-16T00:00:01"
REQUEST="http://localhost:${HTTP_PORT}/tide/tide-stations/${TIDE_STATION}/wh?from=${NOW}&to=${NOW}"
HEADER="Content-Type: application/json"
# Payload is optional
# PAYLOAD="{ \"timezone\": \"America/Los_Angeles\", \"step\": 10, \"unit\": \"feet\" }"
#
if [[ "${VERBOSE}" == "true" ]]
then
  echo -e "Request ${REQUEST}"
  echo -e "Headers ${HEADER}"
  echo -e "Payload ${PAYLOAD}"
fi
#
COMMAND=$(echo curl --location --request POST "'${REQUEST}'" --header "'${HEADER}'" --data-raw "'${PAYLOAD}'")
#
if [[ "${VERBOSE}" == "true" ]]
then
  echo -e "--------------------------------------"
  echo -e "Executing :"
  echo ${COMMAND}
  echo -e "--------------------------------------"
fi
#
# curl --location --request POST "'${REQUEST}'" --header "'${HEADER}'" --data-raw "'${PAYLOAD}'"
# echo curl --location --request POST "'${REQUEST}'" --header "'${HEADER}'" --data-raw "'${PAYLOAD}'" | bash
echo ${COMMAND} | bash
