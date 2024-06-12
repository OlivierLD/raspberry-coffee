#!/bin/bash
IF=$1
if [[ "$IF" == "" ]]; then
  IF=wlan0
fi
ifconfig $IF | grep -oE "inet \b([0-9]{1,3}\.){3}[0-9]{1,3}\b"
