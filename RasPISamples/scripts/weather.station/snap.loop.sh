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
WEATHER_STATION_IP=192.168.42.18
#
while true
do
  # If needed, add rot parameter (default is 0)
  # Send HTTP request to take the snapshot
  curl http://$WEATHER_STATION_IP:8080/snap?rot=270
  NOW=$(date +"%Y_%m_%d_%H_%M_%S")
  IMG_NAME=snap-$NOW.jpg
  # Download the snapshot from the Raspberry Pi
  sshpass -p 'pi' scp pi@$WEATHER_STATION_IP:~/raspberry-coffee/RasPISamples/web/snap-test.jpg ./web/$IMG_NAME
  # open web
  echo see $IMG_NAME in the 'web' directory.
  #
  # Base64 encoding of the downloaded image
  java -cp ${CP} weatherstation.ImageEncoder web/$IMG_NAME > web/encoded.txt
  #
  PROXY=
  # Proxy for REST
  # PROXY="-Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
  #
  # Upload encoded image to the IoT server
  java -cp ${CP} $PROXY -Dkey=54c2767878ca793f2e3cae1c45d62aa7ae9f8056 weatherstation.POSTImage web/encoded.txt
  #
  sleep 600 # 600s: 10 minutes
done
#
