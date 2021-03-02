#!/usr/bin/env bash
echo -e "--- Serial port finder ---"
echo -e "First, UNPLUG the serial device you want the port of from the Raspberry Pi USB port."
echo -e "Hit [return] when done."
read a
ls -l /dev/tty* > before.txt
echo -e "Now, PLUG IN the serial device you want the port of on one of the Raspberry Pi USB ports."
echo -e "Hit [return] when done."
read a
ls -l /dev/tty* > after.txt
#
echo -e "The new port should be listed below:"
diff after.txt before.txt
echo -e "Done!"
