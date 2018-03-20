#!/bin/bash
#
# Take a snapshot on the HTTPServer/Logger, and download it
#
curl http://192.168.42.6:8080/snap
scp pi@192.168.42.6:~/raspberry-pi4j-samples/RasPISamples/web/snap-test.jpg ./web
open web
#

