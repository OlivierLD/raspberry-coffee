#!/bin/bash
CP=./build/libs/ukulele-1.0-all.jar
LANG=EN
if [ $# -eq 1 ]
then
  if [ $1 = FR ]
  then
    LANG=$1
  fi
fi
OPT=-Dlang=$LANG
echo -e "Key Chord Finder"
java -cp $CP $OPT section.one.KeyChordFinder

