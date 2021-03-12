#!/bin/bash
echo Changing Network Name...
NN=`grep -e '^ssid=' /etc/hostapd/hostapd.conf | awk '{ print substr($1, 6) }'`
echo Current Name is $NN
echo -n 'Do you want to change it y|n ? > '
read resp
if [[ "$resp" = "y" ]]
then
  echo OK, let\'s go.
  echo -n 'What should be the new name ? > '
  read NEW_NN
  sed -i "s/$NN/$NEW_NN/g" /etc/hostapd/hostapd.conf
  echo Reboot is required
else
  echo Operation Canceled
fi
read a
