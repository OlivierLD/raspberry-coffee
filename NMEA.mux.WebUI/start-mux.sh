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
if [ "$_OLED" ]; then
  printf "+---------------+\n"
  printf "| OLED Detected |\n"
  printf "+---------------+\n"
  cd ~pi/NMEADist
  MESS=""
  if [ "$_IP" ]; then
    MESS="$_IP|"
  fi
  MESS="$MESS on Pi-Net| default 192.168.127.1"
  ./ssd1306i2cDisplay.sh "$MESS"
elif [ "$_PAPIRUS" ]; then
  MESS=""
  if [ "$_IP" ]; then
    MESS="$_IP,"
  fi
  MESS="$MESS on Pi-Net, default 192.168.127.1"
  papirus-write "$MESS" --fsize 16
else
  printf ">> NO Screen Detected <<\n"
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
  prointf "No to.mux.sh found\n"
fi
cd -
#
