#!/usr/bin/env bash
#
# Provide the number to trim it down to as prm. Default 500
#
echo Working from `pwd`
#
function dropFirst() {
  for jpg in `ls web/*.jpg`; do
    echo "Dropping $jpg"
    rm $jpg
    break   # Remove just the first one
  done
}
#
SIZE=500
if [[ "$1" != "" ]]; then
  SIZE=$1
fi
#
echo "-- Trimming down the web folder to $SIZE entries --"
NB_JPG=`ls -lisah web/*.jpg | wc -l`
while [[ "$NB_JPG" -gt "$SIZE" ]]; do
	dropFirst
  NB_JPG=`ls -lisah web/*.jpg | wc -l`
done
#
echo "We have $NB_JPG image(s) in the web folder"
echo "---------------"
#
