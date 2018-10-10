#!/bin/bash
#
# For a list of the kill signals:
# Try $> kill -l
#
ps -ef | grep SunFlower | grep -v grep | grep -v killsf | awk '{ print $2 }' > km
for pid in `cat km`
do
  echo Killing process $pid
  sudo kill -15 $pid
done
rm km 
