#!/bin/bash
node server.js &
#
echo -e "+----------------------------- N O T E ------------------------------------------+"
echo -e "| To kill the node server, type:                                                 |"
echo -e "|  kill -9 \$(ps -ef | grep TinyNodeServer | grep -v grep | awk '{ print \$2 }')   |"
echo -e "+--------------------------------------------------------------------------------+"
#
echo -e "Open http://localhost:8080/index.html in a browser..."
DARWIN=$(uname -a | grep Darwin)
if [[ "$DARWIN" != "" ]]; then
	echo Running on Mac
  open http://localhost:8080/index.html
else
  echo Assuming Linux/Raspberry Pi
  chromium-browser http://localhost:8080/index.html
fi
