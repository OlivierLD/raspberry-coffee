#!/bin/bash
#
# Starts a SunFlower server for a looooong run.
#
# This can be run on any Linux machine, not only a Raspberry Pi.
# If not on a Raspberry Pi, then the servo part will be ignored,
# but the Sun data [art remains (hence its interest).
#
echo Starting Sun Data REST Server
nohup ./run.sh resthttp &
echo Done
ADDR=$(hostname -I)
TRIM="$(echo -e "${ADDR}" | tr -d '[:space:]')"
echo -e "-----------------------------------------------"
echo -e "try http://${TRIM}:9997/web/sun.data.html"
echo -e "-----------------------------------------------"
