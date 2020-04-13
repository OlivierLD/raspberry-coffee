#!/bin/bash
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
if [[ "$1" == "-w" ]] # To wait for everything to start?
then
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
if [[ "$MY_IP" == "" ]]
then
  MY_IP="192.168.50.10" # Change as needed
fi
#
NETWORK_NAME=$(iwconfig | grep wlan0 | awk '{ print $4 }')
NETWORK_NAME=${NETWORK_NAME:6}
if [[ "$NETWORK_NAME" == "aster" ]] # From Mode:Master TODO Get it from the config file...
then
	NETWORK_NAME="RPi-Gateway" # Change as needed
fi
if [[ "$NETWORK_NAME" == "" ]]
then
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
if [[ -f "to.mux.sh" ]]
then
  printf "+----------------------+\n"
  printf "| Starting Multiplexer |\n"
  printf "+----------------------+\n"
  if [[ "$1" != "" ]]
  then
    echo -e ">> Script param: $1 <<"
  else
    echo ">> No Script prm <<"
  fi
  # See the script for option details, $1 can be --no-background
  ./to.mux.sh -n --no-date $1
else
  printf "No to.mux.sh found\n"
fi
# cd -
#
