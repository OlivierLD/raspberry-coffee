#!/usr/bin/env bash
#
# to be invoked from /etc/rc.local
#
if [[ "$1" == "-w" ]]; then # To wait for everything to start?
	echo -e ""
	echo -e "+-------------------------------+"
	echo -e "| Giving Multiplexer some slack |"
	echo -e "+-------------------------------+"
	sleep 20
fi
#
if [[ -f "to.mux.sh" ]]; then
  printf "+----------------------+\n"
  printf "| Starting Multiplexer |\n"
  printf "+----------------------+\n"
  # See the script for option details
  ./to.mux.sh -n --no-date
else
  printf ">> WARNING:\n"
  printf "No to.mux.sh found\n"
fi
#
