#!/bin/bash
REST_IP=192.168.42.36
REST_PORT=8080
REST_URL=http://${REST_IP}:${REST_PORT}/eink2_13/display
#
echo -e "Require the Python server to be running somewhere (like on ${REST_IP}):"
echo -e "$ python3 src/main/python/eink-2.13/server/eink_2.13_server.py --machine-name:${REST_IP} [ --port:8888 --verbose:true ]"
echo -e " "
#
echo -e "Requests will be POSTed to ${REST_URL}"
#
KEEP_LOOPING=true
#
while [[ "$KEEP_LOOPING" == "true" ]]; do
  echo -n "You say ? ('Q' to quit) > "  # Anything else than 'Q' will keep looping, with the data provided here as payload.
  read text
  if [[ "$text" == "Q" ]] || [[ "$text" == "q" ]]; then
    KEEP_LOOPING=false
  else
    curl --location --request POST "${REST_URL}" \
         --header 'Content-Type: text/plain' \
         --data-raw "${text}" | jq
  fi
done
#
echo -e "Bye!"
