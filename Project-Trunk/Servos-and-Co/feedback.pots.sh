#!/bin/bash
echo -e "The real stuff, knob, feedback, and servo"
#
CP=./build/libs/Servos-and-Co-1.0-all.jar
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Ddisplay.digit=false"
JAVA_OPTS="$JAVA_OPTS -Ddebug=true"
#
sudo java -cp ${CP} feedback.v1.FeedbackPotsServo $*
#
echo Done.
