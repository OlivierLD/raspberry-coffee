#!/bin/bash
CP=./build/libs/SteppersPlayground-1.0-all.jar
#
echo -e "Stepper Motor Demo (WIP)"
#
RPM=30
if [[ $# -eq 1 ]]
then
  RPM=$1
fi
#
echo Revolution per minute set to ${RPM}
#
OPTS=
OPTS="$OPTS -Drpm=$RPM"
OPTS="$OPTS -Dsteps=200"
OPTS="$OPTS -Dhat.debug=false"
#
sudo java -cp ${CP} ${OPTS} motorhat.StepperDemo
