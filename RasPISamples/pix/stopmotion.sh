#!/bin/bash
#
echo To drive and orient the camera::
echo Start in the node directory:
echo   Prompt\> node server.js
echo   and in a browser, access http://localhost:9876/data/servo.pilot.html \(uses tilt.pan.client.js\)
echo The servos are driven by raspisamples.PanTiltWebSocket.java \(script pantilt.ws\)
#
nap=60
echo Will take a snapshot every ${nap} seconds...
keepLooping=true
i=0
while [[ "$keepLooping" = "true" ]]
do
  fname=`printf "../node/pix/snap_%05d.png" $i`
  echo Taking snapshot ${fname}
  raspistill -rot 270 --timeout 1 --nopreview --output ${fname}
  # raspistill -rot 180 --width 200 --height 150 --timeout 1 --nopreview --output $fname
  i=$((i+1))
  sleep ${nap}
done
echo Done!
