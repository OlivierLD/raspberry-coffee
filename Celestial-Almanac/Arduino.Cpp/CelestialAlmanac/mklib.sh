#!/usr/bin/env bash
#
# Archiving AstroLib
#
echo -e "+--------------------+"
echo -e "| Archiving AstroLib |"
echo -e "+--------------------+"
rm AstgroLib/*.o
zip -r AstroLib.zip AstroLib/
#
echo -e "You can now un-archive AstroLib.zip into your Arduino libraries directory"
echo -e "or use the Sketch > Include Library > Add .ZIP Library... in the Arduino IDE menu"
#
