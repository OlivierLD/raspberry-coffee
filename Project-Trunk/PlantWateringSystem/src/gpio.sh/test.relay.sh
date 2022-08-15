#!/bin/bash
#
# Could not get it to work...
#
echo "+-------------------------+"
echo "| testing relay with gpio |"
echo "+-------------------------+"
#
gpio -v
gpio readall
#
PIN=0
echo -e "We will use pin ${PIN} (column wPi)"
#
echo "Enter Q at the prompt to quit"
#
EXIT=false
while [[ "${EXIT}" == "false" ]]; do
  echo "Reading pin #${PIN}: "`gpio read ${PIN}`
  echo -n "Enter new value for the pin (1 or 0) > "
  read NEW_VALUE
  if [[ "${NEW_VALUE}" == "Q" ]] || [[ "${NEW_VALUE}" == "q" ]]; then
    EXIT=true
  else
    gpio write ${PIN} ${NEW_VALUE}
  fi
done
echo "Bye"
#
