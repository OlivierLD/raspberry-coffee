#!/bin/bash
function nocase() {
  if [ "`echo $1 | tr [:lower:] [:upper:]`" = "`echo $2 | tr [:lower:] [:upper:]`" ]
  then
    return 0  # true
  else
    return 1 # false
  fi
}
exit=FALSE
while [ "$exit" = "FALSE" ]
do
  clear
  echo -------------------------------
  echo Bilge Oil Level Detector
  echo -------------------------------
  echo -e 'N: Node Server    '
  echo -e 'O: Oil Detector   '
  echo -e 'Q: Quit           '
  echo -n 'You choose > '
  read opt
  if nocase "$opt" "N"
  then
    cd node
    node server.js &
    cd ..
    read a
  elif nocase "$opt" "O"
  then
    exit=TRUE
    ./run.ws
    read a
  elif nocase "$opt" "Q"
  then
    exit=TRUE
  else
    echo Ah ben merde
  fi
done

