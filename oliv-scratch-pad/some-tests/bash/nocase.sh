#!/bin/bash
#
# Good doc at https://linuxhint.com/bash_lowercase_uppercase_strings/
#
function nocase() {
  if [ "`echo $1 | tr [:lower:] [:upper:]`" = "`echo $2 | tr [:lower:] [:upper:]`" ]
  then
    return 0  # true
  else
    return 1 # false
  fi
}
#
exit=FALSE
while [ "$exit" = "FALSE" ]
do
  clear
  echo -e "+---------------- Dummy Menu -------------------+"
  echo -e '| N: Start Node server                          |'
  echo -e '| W: Start Weather Station reader               |'
  echo -e '| D: Start Weather Station dump                 |'
  echo -e '| S: Show processes                             |'
  echo -e '| K: Kill them all                              |'
  echo -e "+-----------------------------------------------+"
  echo -e '| Q: Quit                                       |'
  echo -e "+-----------------------------------------------+"
  echo -n 'You Choose > '
  read a
  if nocase "$a" "Q"
  then
    exit=TRUE
    echo -ne "Exiting... (hit [Return]) "
    read dummy
  else
    echo -ne "You typed ${a}, same as $(echo $a | tr [:lower:] [:upper:]) (hit [Return]) "
    read dummy
  fi
done
