#!/bin/bash
#
echo -e "This requires a Multiplexer to be running, with a given HTTP Port"
PRMS=""
echo -en " ==> Enter Multiplexer machine name or IP (default 'localhost'): "
read MACHINE_NAME
if [[ "${MACHINE_NAME}" != "" ]]; then
    PRM="${PRM}--machine-name:${MACHINE_NAME} "
fi
echo -en " ==> Enter Multiplexer HTTP port (default 9999): "
read HTTP_PORT
if [[ "${HTTP_PORT}" != "" ]]; then
    PRM="${PRM}--port:${HTTP_PORT} "
fi
echo -en " ==> Enter REST query (default '/mux/cache'): "
read REST_QUERY
if [[ "${REST_QUERY}" != "" ]]; then
    PRM="${PRM}--query:${REST_QUERY} "
fi
echo -en " ==> With verbose option (default false): "
read VERBOSE
if [[ "${VERBOSE}" != "" ]]; then
    if [[ ${VERBOSE} =~ ^(yes|y|Y)$ ]]; then
        VERBOSE=true
    fi
    if [[ "${VERBOSE}" != "" ]]; then
        PRM="${PRM}--verbose:${VERBOSE} "
    fi
fi
#
COMMAND="./httpClient ${PRM}"
echo -e "Command will be: ${COMMAND}"
RESP="n"
if [[ "$(which jq)" != "" ]]; then
    echo -en "With jq y|n > ? "
    read RESP
fi
if [[ ${RESP} =~ ^(yes|y|Y)$ ]]; then
   ${COMMAND} | jq
else
   ${COMMAND} 
fi
# echo -e "\nHit [Return]"
# read resp
