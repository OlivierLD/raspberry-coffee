#!/usr/bin/env bash
#
# Take a snapshot on the HTTPServer/Logger, and download it
#
PATH=$PATH:/usr/local/bin
# If needed, proxy goes here, for curl
# export http_proxy=http://www-proxy.us.oracle.com:80
# export https_proxy=http://www-proxy.us.oracle.com:80
#
CP=./build/libs/RasPISamples-1.0-all.jar
#
while true
do
  curl http://192.168.42.2:8080/snap
  NOW=$(date +"%Y_%m_%d_%H_%M_%S")
  IMG_NAME=snap-$NOW.jpg
  sshpass -p 'pi' scp pi@192.168.42.2:~/raspberry-pi4j-samples/RasPISamples/web/snap-test.jpg ./web/$IMG_NAME
  # open web
  echo see $IMG_NAME in the 'web' directory.
  #
  java -cp $CP weatherstation.ImageEncoder web/$IMG_NAME > web/encoded.txt
  #
  PROXY=
  # Proxy for REST
  # PROXY="-Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
  #
  java -cp $CP $PROXY -Dkey=[yada-yada-dead-monkey] weatherstation.POSTImage web/encoded.txt
  #
  sleep 600 # 600s: 10 minutes
done
#
