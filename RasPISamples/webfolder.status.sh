#!/usr/bin/env bash
#
echo Working from `pwd`
#
echo "-- Checking the web folder status --"
NB_JPG=`ls -lisah web/*.jpg | wc -l`
echo "There are $NB_JPG image(s) in the web folder"
echo "---------------"
#
