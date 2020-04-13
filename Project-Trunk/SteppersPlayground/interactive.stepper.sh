#!/bin/bash
CP=./build/libs/SteppersPlayground-1.0-all.jar
#
echo -e "Stepper Motor Demo (WIP)"
#
OPTS=
OPTS="$OPTS -Dmotor.hat.verbose=false"
OPTS="$OPTS -Dhat.debug=false"
#
sudo java -cp ${CP} ${OPTS} motorhat.InteractiveStepper
