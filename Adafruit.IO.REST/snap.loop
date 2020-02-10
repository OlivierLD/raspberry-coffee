#!/bin/bash
#
# To run on the Raspberry Pi.
# For camera (raspistill) doc, see https://www.raspberrypi.org/documentation/raspbian/applications/camera.md
# to get the image: scp pi@192.168.42.XX:~/snap.jpg snap.jpg
#
while :
do
  # raspistill -rot 0 --width 300 --height 225 --timeout 1 --output snap.jpg --nopreview
  raspistill -rot 0 --width 300 --height 225 --output snap.jpg --nopreview
  sleep 10
done

