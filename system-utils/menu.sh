#!/bin/bash
function nocase() {
  if [[ "$(echo $1 | tr [:lower:] [:upper:])" == "$(echo $2 | tr [:lower:] [:upper:])" ]]; then
    return 0  # true
  else
    return 1 # false
  fi
}

exit=FALSE
while [[ "${exit}" == "FALSE" ]]; do
  clear
  echo -e "-------------------------------"
  echo -e "This is the script named '${0}'"
  echo -e "-------------------------------"
  echo -e 'S: Show Network config '
  echo -e 'I: IP Address '
  echo -e 'C: Change Network config'
  echo -e 'N: Change Network Name'
  echo -e 'Q: Quit           '
  echo -n 'You choose > '
  read opt
  if nocase "${opt}" "S" ; then
    ./network.status.sh
    echo -en "Hit [return]"
    read a
  elif nocase "${opt}" "I" ; then
    ./ip.sh
    read a
  elif nocase "${opt}" "C"; then
    sudo ./switch.sh
  elif nocase "${opt}" "N" ; then
    sudo ./change.network.name.sh
  elif nocase "${opt}" "Q"; then
    exit=TRUE
  else
    echo -e "Ah ben merde"
  fi
done

