#!/bin/bash
function nocase() {
  if [[ "`echo $1 | tr [:lower:] [:upper:]`" = "`echo $2 | tr [:lower:] [:upper:]`" ]]
  then
    return 0 # true
  else
    return 1 # false
  fi
}
echo Switch Network Config
echo '+---------------------------------------+'
echo '| Type AH for Ad-hoc                    |'
echo '| Type AP for Access Point              |'
echo '| Type N to connect to existing network |'
echo '+---------------------------------------+'
echo ''
echo -n 'You choose > '
read choice
if nocase "$choice" "AH"
then
  echo Switching to AD-HOC
  ./switch-to-ad-hoc Ad-Hoc
  sudo cp /etc/default/hostapd.no /etc/default/hostapd
  echo -n 'Reboot is required, reboot now y|n ? > '
  read a
  if nocase "$a" "Y"
  then
    sudo reboot
  fi
elif nocase "$choice" "AP"
then
  echo Switching to Access Point
  ./switch-to-ad-hoc Access-Point
  sudo cp /etc/default/hostapd.yes /etc/default/hostapd
  echo -n 'Reboot is required, reboot now y|n ? > '
  read a
  if nocase "$a" "Y"
  then
    sudo reboot
  fi
elif nocase "$choice" "N"
then
  echo Switching to NORMAL
  ./switch-to-normal
  echo -n 'Reboot is required, reboot now y|n ? > '
  read a
  if nocase "$a" "Y"
  then
    sudo reboot
  fi
else
  echo Unknown Command [$choice]
fi

