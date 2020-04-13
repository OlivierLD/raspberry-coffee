#!/bin/bash
#
# 2 Scripts writing in the same file.
#
rm out.txt 2> /dev/nul
./script.1.sh >> out.txt &
sleep 0.5
./script.2.sh >> out.txt &
#
tail -f out.txt
