#!/usr/bin/env bash
#
widgets=("analogdisplay" "analogwatch" "calendar" "compass" "direction" "jumbo" "ledpanel" "marquee" "rain" "raw" "skymap" "splitflap" "temperature" "windangle" "worldmap")
#
HOME=$PWD
echo -e "Working from $HOME"
#
for dir in "${widgets[@]}"
do
  echo -e "-------------------------"
  echo -e "Processing ${dir}"
  echo -e "-------------------------"
  cd $dir
  #
  yarn
  yarn build
  #
  cd ..
done
#
echo -e "Done"
