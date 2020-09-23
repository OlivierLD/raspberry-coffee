#!/usr/bin/env bash
# Makes the link from /dev/ttyACM0 to /dev/ttyS80, in case you see a "Port not found" on /dev/ttyACM0
# Then you should read /dev/ttyS80
echo -e "------------------------------------"
echo -e " 1 - Map /dev/ttyACM0 to /dev/ttyS80"
echo -e " 2 - Map /dev/ttyAMA0 to /dev/ttyS80"
echo -e " Q - Quit"
echo -e "------------------------------------"
echo -en "You choose > "
read choice
case "$choice" in
	  "1")
      sudo ln -s /dev/ttyACM0 /dev/ttyS80
      ll /dev/ttyS80
	    ;;
	  "2")
      sudo ln -s /dev/ttyAMA0 /dev/ttyS80
      ll /dev/ttyS80
	    ;;
	  "Q"|"q")
	    ;;
	  *)
	    echo -e "Unknown Option. Only Q, 1 or 2".
      ;;
esac
echo -e "Done"
#
