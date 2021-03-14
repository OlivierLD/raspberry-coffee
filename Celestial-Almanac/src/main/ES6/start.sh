#!/bin/bash
node server.js &
#
echo -e "+----------------------------- N O T E ------------------------------------------+"
echo -e "| To kill the node server, type:                                                 |"
echo -e "|  kill -9 \$(ps -ef | grep TinyNodeServer | grep -v grep | awk '{ print \$2 }') |"
echo -e "+--------------------------------------------------------------------------------+"
#
open http://localhost:8080/index.html
