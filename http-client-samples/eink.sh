#!/bin/bash
REST_URL=http://192.168.42.36:8080/eink2_13/display
echo -e "Requests will be POSTed to ${REST_URL}"
#
KEEP_LOOPING=true
#
while [[ "$KEEP_LOOPING" == "true" ]]
do
  echo -n "You say ? ('Q' to quit) > "
  read text
  if [[ "$text" == "Q" ]]
  then
    KEEP_LOOPING=false
  else
    curl --location --request POST "${REST_URL}" \
         --header 'Content-Type: text/plain' \
         --data-raw "${text}" | jq
  fi
done
#
echo -e "Bye!"
