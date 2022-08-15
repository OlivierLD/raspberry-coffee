#!/bin/bash
#
# Used to take snapshots of the device during the day.
# Can be used afterwards to make an animated gif.
#
nap=60
echo Will take a snapshot every ${nap} seconds...
#
rm -rf ./pix 2> /dev/null
mkdir pix
#
keepLooping=true
i=0
while [[ "$keepLooping" = "true" ]]; do
  fname=`printf "./pix/snap_%05d.png" $i`
  echo Taking snapshot ${fname}
  # raspistill -rot 270 --timeout 1 --nopreview --output ${fname}
  raspistill -rot 180 --width 800 --height 600 --timeout 1 --nopreview --output $fname
  i=$((i+1))
  sleep ${nap}
done
echo Done!
