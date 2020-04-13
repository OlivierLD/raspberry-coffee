#!/usr/bin/env bash
# Makes the link from /dev/ttyACM0 to /dev/ttyS80, in case you see a "Port not found" on /dev/ttyACM0
# Then you should read /dev/ttyS80
sudo ln -s /dev/ttyACM0 /dev/ttyS80
#
