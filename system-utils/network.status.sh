#!/bin/bash
echo Machine Name: $(hostname)
echo Your IP Address:
echo -----------------------------------------------
ifconfig | grep 'inet addr'
echo -----------------------------------------------
if grep -q "ad-hoc" /etc/network/interfaces
then
  if grep -q "yes" /etc/default/hostapd ; then
    echo Network config is access point.
  else
    echo Network config is ad-hoc.
  fi
else
  echo network config is normal. You can connect to existing network.
fi

