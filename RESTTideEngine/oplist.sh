#!/bin/bash
# sleep 10
#
if [[ "${HTTP_PORT}" == "" ]]; then
  HTTP_PORT=8080
fi
OPTION="ONE"
#
if [[ "$1" != "" ]]; then
  OPTION=$1
fi
# echo Invoking http://localhost:${HTTP_PORT}/tide/oplist
if [[ "${OPTION}" == "ONE" ]]; then
  curl -X GET http://localhost:${HTTP_PORT}/tide/oplist | jq '.[] | { verb, path, description }'
elif [[ "${OPTION}" == "TWO" ]]; then
  curl -X GET http://localhost:${HTTP_PORT}/tide/oplist | jq '.[] | { verb, path } | join(" ")'
else
  echo -e "Watafok ??! Supported options are ONE and TWO. For now."
fi
