#!/bin/bash
#
# Take a snapshot on the HTTPServer/Logger, and download it
#
PATH=$PATH:/usr/local/bin
# If needed, proxy goes here
# export http_proxy=http://www-proxy.us.oracle.com:80
# export https_proxy=http://www-proxy.us.oracle.com:80
#
curl http://192.168.42.2:8080/snap
sshpass -p 'pi' scp pi@192.168.42.2:~/raspberry-pi4j-samples/RasPISamples/web/snap-test.jpg ./web
open web
#

