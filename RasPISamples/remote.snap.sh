#!/bin/bash
#
# Take a snapshot on the HTTPServer/Logger, and download it
#
PATH=$PATH:/usr/local/bin
curl http://192.168.42.6:8080/snap
sshpass -p 'pi' scp pi@192.168.42.6:~/raspberry-pi4j-samples/RasPISamples/web/snap-test.jpg ./web
open web
#

