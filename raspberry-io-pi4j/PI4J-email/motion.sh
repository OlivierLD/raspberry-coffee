#!/bin/bash
SLEEP=10
if [ $# -eq 1 ]
then
  SLEEP=$1
  echo $SLEEP sec bewteen loops
fi
while true 
do
  # echo Snap!
  raspistill --nopreview --output snap.jpg  -w 640 -h 480 --quality 5 -rot 0 # --verbose 
  sleep $SLEEP
done

