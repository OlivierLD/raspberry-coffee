#!/usr/bin/env bash
#
# Take a snapshot on the HTTPServer/Logger, and download it
#
PATH=$PATH:/usr/local/bin
# If needed, proxy goes here
# export http_proxy=http://www-proxy.us.oracle.com:80
# export https_proxy=http://www-proxy.us.oracle.com:80
#
CP=./build/libs/RasPISamples-1.0-all.jar
#
while true
do
  curl http://192.168.42.2:8080/snap
  sshpass -p 'pi' scp pi@192.168.42.2:~/raspberry-pi4j-samples/RasPISamples/web/snap-test.jpg ./web
  # open web
  echo see the 'web' directory.
  #
  java -cp $CP weatherstation.ImageEncoder web/snap-test.jpg > web/encoded.txt
  #
  PROXY=
  PROXY="-Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
  #
  java -cp $CP $PROXY -Dkey=[yada-yada-dead-monkey] weatherstation.POSTImage web/encoded.txt
  #
  sleep 600 # 600: 10 minutes
done
#
