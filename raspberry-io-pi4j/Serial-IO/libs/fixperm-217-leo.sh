#!/bin/sh

# A script to fix permissions for lock files on Mac OS X
# Contributed by Dmitry Markman <dimitry.markman@verizon.net>
# Fri Aug 23 15:46:46 MDT 2002
# Adapted for RXTX 2.1-7 by LA3HM 30 Jul 2006
# Replaced 'niutil' with 'dscl' for Leopard 

curruser=`sudo id -p | grep 'login' | sed 's/login.//'`

if [ ! -d /var/lock ]
then
sudo mkdir /var/lock
fi

sudo chgrp uucp /var/lock
sudo chmod 775 /var/lock
#if [ ! `sudo niutil -readprop / /groups/uucp users | grep $curruser > /dev/null` ]
if [ ! `sudo dscl . -read / /groups/_uucp users | grep $curruser > /dev/null` ]
then
#  sudo niutil -mergeprop / /groups/uucp users $curruser
  sudo dscl . -append /groups/_uucp GroupMembership $curruser
fi
