#!/usr/bin/env bash
#
# to be invoked from /etc/rc.local
#
# _OLED=`i2cdetect -y 1 | grep 3c`
# _PAPIRUS=`i2cdetect -y 1 | grep 48`
#
_OLED=$(i2cdetect -y 1 | grep 3c) || true
_PAPIRUS=$(i2cdetect -y 1 | grep 48) || true
#
# echo "OLED: $_OLED"
# echo "PAPIRUS: $_PAPIRUS"
#
if [[ "$1" == "-w" ]]; then  # To wait for everything to start?
	echo -e ""
	echo -e "+-------------------------------+"
	echo -e "| Giving Multiplexer some slack |"
	echo -e "+-------------------------------+"
	sleep 10
	echo -e ""
	echo -e "+--------------------------+"
	echo -e "| Now starting Multiplexer |"
	echo -e "+--------------------------+"
fi
#
MY_IP=$(hostname -I | awk '{ print $1 }')
if [[ "${MY_IP}" == "" ]]
then
  MY_IP="192.168.50.10" # Change as needed
fi
#
NETWORK_NAME=$(iwconfig | grep wlan0 | awk '{ print $4 }')
NETWORK_NAME=${NETWORK_NAME:6}
if [[ "$NETWORK_NAME" == "aster" ]]; then   # From Mode:Master Get the network name from the config file... ssid in /etc/hostapd/hostapd.conf
	NETWORK_NAME=$(cat /etc/hostapd/hostapd.conf | grep -e ^ssid)
	NETWORK_NAME=${NETWORK_NAME:5}
fi
if [[ "$NETWORK_NAME" == "" ]]; then
  NETWORK_NAME="RPi-Gateway" # Change as needed
fi
#
if [[ "$_OLED" ]]; then
  printf "+---------------+\n"
  printf "| OLED Detected |\n"
  printf "+---------------+\n"
# cd ~pi/NMEADist
  MESS=""
  if [[ "$_IP" ]]; then
    MESS="$_IP|"
  fi
  MESS="$MESS on $NETWORK_NAME| IP $MY_IP"
  ./ssd1306i2cDisplay.sh "$MESS"
elif [[ "$_PAPIRUS" ]]; then
  MESS=""
  if [[ "$_IP" ]]; then
    MESS="$_IP,"
  fi
  MESS="$MESS on $NETWORK_NAME, IP $MY_IP"
  papirus-write "$MESS" --fsize 16
else
  printf ">> NO Screen Detected <<\n"
  printf "On $NETWORK_NAME, IP $MY_IP\n"
fi
#
# cd ~pi/NMEADist
if [[ -f "to.mux.sh" ]]; then
  printf "+----------------------+\n"
  printf "| Starting Multiplexer |\n"
  printf "+----------------------+\n"
  # See the script for option details
  ./to.mux.sh -n --no-date
else
  printf "No to.mux.sh found\n"
fi
# cd -
#
