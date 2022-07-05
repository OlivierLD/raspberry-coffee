#!/bin/bash
#
# For a list of the kill signals:
# Try $> kill -l
#
SUDO=
# DARWIN=`uname -a | grep Darwin`
DARWIN=$(uname -a | grep Darwin)
#
if [[ "$DARWIN" != "" ]]; then
	echo -e "Running on Mac"
else
	echo -e "Assuming Linux/Raspberry Pi"
  # No sudo require if running as root, in Docker for example.
  if [[ "$(whoami)" != "root" ]]; then
    SUDO="sudo "
  fi
fi
#
ps -ef | grep TideServer | grep -v grep | grep -v killts | awk '{ print $2 }' > km
NB_L=`cat km | wc -l`
if [[ ${NB_L} == 0 ]]; then
  echo -e "No TideServer process found."
fi
for pid in $(cat km) ; do
  echo -e "Killing process ${pid}"
  ${SUDO} kill -15 ${pid}
done
rm km

