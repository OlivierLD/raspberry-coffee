#!/usr/bin/env bash
echo Usage:
echo  $0 [AIO Username] [AIO Key]
echo like $0 olivierld abc8736hgfd78638620ngs
if [ $# -eq 2 ]
then
  CP=./build/libs/Monitor.Battery-1.0-all.jar
  sudo java -Dverbose=false -Daio.user.name=$1 -Daio.key=$2 -cp ${CP} battery.mqtt.pub.AIOPublisher
else
  echo Please provide the expected 2 parameters.
fi
#
