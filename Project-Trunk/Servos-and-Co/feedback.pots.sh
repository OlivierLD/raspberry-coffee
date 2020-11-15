#!/bin/bash
echo -e "The real stuff, knob, feedback, and servo"
#
CP=./build/libs/Servos-and-Co-1.0-all.jar
#
JAVA_OPTS=
JAVA_OPTS="$JAVA_OPTS -Ddisplay.digit=false"
JAVA_OPTS="$JAVA_OPTS -Ddebug=true"
#
# Parameters are --servo-channel:1 --knob-channel:0 --feedback-channel:1 \
#                --servo-freq:100 --servo-stop-pwm:603 --servo-forward-pwm:595 \
#                --servo-backward-pwm:632 --min-diff:6
#
sudo java -cp ${CP} ${JAVA_OPTS} feedback.v1.FeedbackPotsServo $*
#
echo Done.
