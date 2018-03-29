#!/bin/bash
#
# Take a snapshot on the HTTPServer/Logger, and download it
# Supported prms: -rot:270 -width:640 -height:480 -name:snap-test
#
PATH=$PATH:/usr/local/bin
# If needed, proxy goes here
# export http_proxy=http://www-proxy.us.oracle.com:80
# export https_proxy=http://www-proxy.us.oracle.com:80
#
# rot: default is 0
# TODO name, width & height
#
ROT=270
WIDTH=640
HEIGHT=480
NAME="snap-test"
#
for prm in $*
do
  if [[ $prm == "-rot:"* ]]
  then
    ROT=${prm#*:}
  elif [[ $prm == "-width:"* ]]
  then
    WIDTH=${prm#*:}
  elif [[ $prm == "-height:"* ]]
  then
    HEIGHT=${prm#*:}
  elif [[ $prm == "-name:"* ]]
  then
    NAME=${prm#*:}
  else
    echo "Unsupported parameter $prm"
  fi
done
#
echo "Using rot:$ROT, width=$WIDTH, height=$HEIGHT, name=$NAME"
#
curl http://192.168.42.2:8080/snap?rot=$ROT&width=$WIDTH&height=$HEIGHT&name=$NAME
sshpass -p 'pi' scp pi@192.168.42.2:~/raspberry-pi4j-samples/RasPISamples/web/$NAME.jpg ./web
# open web
#
