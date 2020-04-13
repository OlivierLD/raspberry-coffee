#!/usr/bin/env bash
#
echo -en "You want to kill the server [n]|y ? > "
read resp
if [ "$resp" == "y" ] || [ "$resp" == "Y" ]
then
  echo -e "Let's go!"
  sudo kill $(ps -ef | grep httpserver.HttpRequestServer | grep -v grep | awk '{ print $2 }')
else
  echo -e "Wow! That was close, hey?"
fi
