#!/bin/bash
sleep 10
#
echo Invoking http://localhost:${HTTP_PORT}/tide/oplist
curl -X GET http://localhost:${HTTP_PORT}/tide/oplist | jq
