#!/bin/bash
cp /etc/network/interfaces.backup /etc/network/interfaces
ifdown --force wlan0
ifup wlan0
echo -e "Ready for normal config (non ad-hoc)"
echo -e "You can connect to an existing network"
