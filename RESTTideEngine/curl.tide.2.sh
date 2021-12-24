#!/bin/bash
#
# Example:
# Query the water height for Port-Tudy for the current time.
#
# Use it with jq if VERBOSE == false (see below).
#
# jq cheatsheet https://lzone.de/cheat-sheet/jq
# date formats: https://www.cyberciti.biz/faq/linux-unix-formatting-dates-for-display/
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
ESC_TIDE_STATION=$(echo ${TIDE_STATION/,/%2C})
ESC_TIDE_STATION=$(echo ${ESC_TIDE_STATION/ /%20})
# TIDE_STATION="Port-Tudy%2C%20France"  # Escaped for "Port-Tudy, France"
# Dates, Duration format.
NOW=$(date +"%Y-%m-%dT%T")
SHORT_DATE=$(date +"%a %d-%m-%Y %H:%M")
#
REQUEST=$(echo -e "http://localhost:${HTTP_PORT}/tide/tide-stations/${ESC_TIDE_STATION}/wh?from=${NOW}&to=${NOW}")
CONTENT_TYPE_HEADER="Content-Type: application/json"
# Payload is optional
# UNIT="feet"
UNIT="meter"
PAYLOAD="{ \"timezone\": \"America/Los_Angeles\", \"step\": 10, \"unit\": \"${UNIT}\" }"
#
if [[ "${VERBOSE}" == "true" ]]
then
  echo -e "Request ${REQUEST}"
  echo -e "Headers ${CONTENT_TYPE_HEADER}"
  echo -e "Payload ${PAYLOAD}"
fi
#
COMMAND=$(echo curl --location --request POST "'${REQUEST}'" --header "'Content-Type: ${CONTENT_TYPE_HEADER}'" --data-raw "'${PAYLOAD}'")
#
if [[ "${VERBOSE}" == "true" ]]
then
  echo -e "--------------------------------------"
  echo -e "Executing :"
  echo ${COMMAND}
  echo -e "--------------------------------------"
fi
#
#curl --location --request POST "${REQUEST}" \
#                       --header "Content-Type: ${CONTENT_TYPE_HEADER}" \
#                       --data-raw "${PAYLOAD}"

WH=$(curl --location --request POST "${REQUEST}" \
                     --header "Content-Type: ${CONTENT_TYPE_HEADER}" \
                     --data-raw "${PAYLOAD}" | jq '.heights | objects[] | .wh')
#
echo -e "-----------------------------------------"
echo -en "Water Height in ${TIDE_STATION} on ${SHORT_DATE}: "
printf "%03.2f " ${WH}
echo -e "(in ${UNIT})"
echo -e "-----------------------------------------"
