#!/bin/bash
#
# For a list of the kill signals:
# Try $> kill -l
#
ps -ef | grep NavServer | grep -v grep | grep -v killns | awk '{ print $2 }' > km
NB_L=`cat km | wc -l`
if [[ ${NB_L} == 0 ]]
then
  echo No NavServer process found.
fi
for pid in `cat km`
do
  echo Killing process ${pid}
  sudo kill -15 ${pid}
done
rm km
