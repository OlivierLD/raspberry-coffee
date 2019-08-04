#!/bin/bash
CP=./build/libs/ukulele-1.0.jar
LANG=EN
if [ $# -eq 1 ]
then
  if [ $1 = FR ]
  then
    LANG=$1
  fi
fi
OPT=-Dlang=$LANG
echo -e "Big Chord Finder"
java -cp $CP $OPT chordfinder.UkuleleChordFinder

