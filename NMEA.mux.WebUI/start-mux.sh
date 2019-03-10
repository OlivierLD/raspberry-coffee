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
MY_IP=`hostname -I | awk '{ print $1 }'`
NETWORK_NAME=$(iwconfig | grep wlan0 | awk '{ print $4 }')
NETWORK_NAME=${NETWORK_NAME:6}
if [ "$NETWORK_NAME" == "" ]
then
  NETWORK_NAME="This network"
fi
#
if [ "$_OLED" ]; then
  printf "+---------------+\n"
  printf "| OLED Detected |\n"
  printf "+---------------+\n"
# cd ~pi/NMEADist
  MESS=""
  if [ "$_IP" ]; then
    MESS="$_IP|"
  fi
  MESS="$MESS on $NETWORK_NAME| IP $MY_IP"
  ./ssd1306i2cDisplay.sh "$MESS"
elif [ "$_PAPIRUS" ]; then
  MESS=""
  if [ "$_IP" ]; then
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
if [ -f "to.mux.sh" ]
then
  printf "+----------------------+\n"
  printf "| Starting Multiplexer |\n"
  printf "+----------------------+\n"
  # See the script for option details
  ./to.mux.sh -n
else
  printf "No to.mux.sh found\n"
fi
# cd -
#
