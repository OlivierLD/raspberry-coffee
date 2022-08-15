#!/bin/bash
#
# Logger / Runner / Kayak, etc.
#
YES=
if [[ "$1" == "-y" ]]; then
  YES=1
fi
if [[ "$1" == "-n" ]]; then
  YES=0
fi
# Change directory below as needed.
cd raspberry-coffee/NMEA-multiplexer
#
echo -e "Working from $PWD"
#
a=
if [[ "$YES" == "1" ]]; then
  a=y
elif [[ "$YES" == "0" ]]; then
  a=n
else
  echo -en "Remove data.nmea ? y|n > "
  read a
fi
if [[ "$a" = "y" ]]; then
  echo -e "Removing previous log file"
  sudo rm data.nmea
  sudo rm nohup.out
else
  # Rename existing ones
	if [[ -f ./data.nmea ]]; then
    # If data.nmea exits, rename it
    now=`date +%Y-%m-%d.%H:%M:%S`
    echo -e "Renaming previous data file to ${now}_data.nmea"
    sudo mv data.nmea ${now}_data.nmea
  fi
  if [[ -f ./nohup.out ]]; then
    # If nohup.out exits, rename it
    now=`date +%Y-%m-%d.%H:%M:%S`
    echo -e "Renaming previous log file to ${now}_nohup.out"
    sudo mv nohup.out ${now}_nohup.out
  fi
fi
nohup ./mux.sh nmea.mux.gps.log.properties &
echo On its way!
echo "Reach http://192.168.127.1:9999/web/index.html"
echo "  or  http://192.168.127.1:9999/web/runner.html"
date=`date`
echo "System date is $date"
