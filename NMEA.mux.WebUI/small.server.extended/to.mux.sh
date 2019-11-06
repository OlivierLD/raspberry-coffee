#!/bin/bash
#
# Logger / Runner / Kayak, etc.
# This file is to be invoked from /etc/rc.local
#
YES=
if [[ "$1" == "-y" ]]
then
  YES=1
fi
if [[ "$1" == "-n" ]]
then
  YES=0
fi
# >>> Change directory below as needed. <<<
# cd raspberry-coffee/NMEA.multiplexer
a=
if [[ "$YES" == "1" ]]
then
  a=y
elif [[ "$YES" == "0" ]]
then
  a=n
else
  if [[ -f data.nmea ]]
  then
    echo -en "Remove data.nmea ? y|n > "
    read a
  fi
fi
if [[ "$a" = "y" ]]
then
  echo -e "Removing previous log file"
  if [[ -f data.nmea ]]
  then
    sudo rm data.nmea
  fi
  sudo rm nohup.out
else
  # Rename existing ones
	if [[ -f ./data.nmea ]]
  then
    # If data.nmea exits, rename it
    now=`date +%Y-%m-%d.%H:%M:%S`
    echo -e "Renaming previous data file to ${now}_data.nmea"
    sudo mv data.nmea ${now}_data.nmea
  fi
  if [[ -f ./nohup.out ]]
  then
    # If nohup.out exits, rename it
    now=`date +%Y-%m-%d.%H:%M:%S`
    echo -e "Renaming previous log file to ${now}_nohup.out"
    sudo mv nohup.out ${now}_nohup.out
  fi
fi
#
# Script parameters
#
echo -e "+--------------------------------------------------------------------------+"
echo -e "| Usage is:                                                                |"
echo -e "+--------------------------------------------------------------------------+"
echo -e " $0 [-y|-n] [--proxy] [--no-date] [--no-rmc-time] [--mux:properties.file]"
echo -e "+--------------------------------------------------------------------------+"
echo -e "| or                                                                       |"
echo -e "+--------------------------------------------------------------------------+"
echo -e " $0 [-y|-n] [-p] [--no-date] [--no-rmc-time] [-m:properties.file]"
echo -e "+--------------------------------------------------------------------------+"
echo -e "| where:                                                                   |"
echo -e "| -y|-n : if found as first parameter, -y will purge the previous          |"
echo -e "|         log file (if it exists) without prompting you                    |"
echo -e "| --proxy or -p will set a proxy (see in the script)                       |"
echo -e "| --no-date will NOT use GGL or GGA dates (just RMC)                       |"
echo -e "| --no-rmc-time will not be used (useful when replaying data)              |"
echo -e "| --mux or -m will allow you to override the default properties file.      |"
echo -e "|             Can be a properties file or a yaml file                      |"
echo -e "|             Try multiplexer.yaml 😜                                      |"
echo -e "+--------------------------------------------------------------------------+"
#
NO_DATE=false
RMC_TIME_OK=true
SUN_FLOWER=false
# PROP_FILE="nmea.mux.gps.log.properties"
PROP_FILE="multiplexer.yaml"
JAVA_OPTIONS=
#
for ARG in "$@"
do
	echo -e "Managing prm $ARG"
  if [[ "$ARG" == "-p" ]] || [[ "$ARG" == "--proxy" ]]
  then
    USE_PROXY=true
  elif [[ "$ARG" == "--no-date" ]]
  then
    NO_DATE=true
  elif [[ "$ARG" == "--no-rmc-time" ]]
  then
    RMC_TIME_OK=false
  elif [[ ${ARG} == -m:* ]] || [[ ${ARG} == --mux:* ]] # !! No quotes !!
  then
    PROP_FILE=${ARG#*:}
    echo -e "Detected properties file $PROP_FILE"
  fi
done
#
if [[ "$NO_DATE" == "true" ]]
then
	# To use when re-playing GPS data. Those dates will not go in the cache.
	JAVA_OPTIONS="$JAVA_OPTIONS -Ddo.not.use.GGA.date.time=true"
	JAVA_OPTIONS="$JAVA_OPTIONS -Ddo.not.use.GLL.date.time=true"
fi
#
if [[ "$RMC_TIME_OK" == "false" ]]
then
	# To use when re-playing GPS data. Those dates will not go in the cache.
	JAVA_OPTIONS="$JAVA_OPTIONS -Drmc.time.ok=false"
fi
#
echo -e "JAVA_OPTIONS in to.mux.sh: $JAVA_OPTIONS"
# The script below uses $JAVA_OPTIONS (hence the .)
# nohup ./mux.sh $PROP_FILE &
. ./mux.sh ${PROP_FILE} &
#
echo On its way!
MY_IP=$(hostname -I | awk '{ print $1 }')
echo "Reach http://${MY_IP}:9999/web/index.html"
echo "  or  http://${MY_IP}:9999/web/small-screens/small.console.01.html"
echo "- Note: port may change"
date=`date`
echo "System date is $date"
