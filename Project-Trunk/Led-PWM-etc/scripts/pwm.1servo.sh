#!/bin/bash
CP=../build/libs/Led-PWM-etc-1.0-all.jar
#
PIN=12  # Physical pin #
if [[ $# -gt 0 ]]; then
  PIN=$1
fi
echo -e "Using physical pin #$PIN"
#
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dservo.pin=$PIN"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dpwm.debug=true"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dtime.verbose=true"
#
sudo java ${JAVA_OPTIONS} -cp ${CP} tests.RealPWMServo
