#!/usr/bin/env bash
echo Usage:
echo  $0 [AIO Key]
echo like $0 abc8736hgfd78638620ngs
if [ $# -eq 1 ]
then
  CP=./build/libs/Monitor.Battery-1.0.jar
  sudo java -Daio.key=$1 -cp $CP battery.rest.PostVoltage
else
  echo Please provide the expected parameter.
fi
#
