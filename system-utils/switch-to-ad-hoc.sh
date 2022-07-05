#!/bin/bash
cp /etc/network/interfaces.ad-hoc /etc/network/interfaces
ifdown --force wlan0
ifup wlan0
echo -e "ready for $1 network"
