#!/bin/bash
#
# Prompt the user to run any diozero sample app
# requires "implementation 'com.diozero:diozero-sampleapps:1.3.3'" in teh build file.
#
JAR=./build/libs/diozero-101-1.0-all.jar
#
jar -tvf ./build/libs/diozero-101-1.0-all.jar | grep com/diozero/sampleapps/.*\.class | awk '{ print substr($8, 24, length($8)-29) }' > samples.txt
#
NL=1
for line in $(cat samples.txt); do
  echo -e "${NL} - ${line}"
  NL=$(expr ${NL} + 1)
done
#
echo -en "The number you choose > "
read LINE_NO
echo -e "OK, line ${LINE_NO}"
#
CLASS_NAME=
NL=1
for line in $(cat samples.txt); do
  if [[ "${NL}" == "${LINE_NO}" ]]; then
    CLASS_NAME=${line}
    break
  fi
  NL=$(expr ${NL} + 1)
done
#
echo -e "You choose ${CLASS_NAME}"
if [[ "${CLASS_NAME}" != "" ]]; then
  # Then execute
  # Ask for extra parameter(s)
  echo -en "Any extra prm(s) ? > "
  read PRMS
  java -cp ${JAR} com.diozero.sampleapps.${CLASS_NAME} ${PRMS}
else
  echo -e "Try again..."
fi

