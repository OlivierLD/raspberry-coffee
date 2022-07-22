#!/bin/bash
CP=./build/libs/diozero-101-1.0-all.jar
#
echo -e "+-------------------------------------------------------+"
echo -e "|        D I O Z E R O   B A S I C   T E S T S          |"
echo -e "+---------------------------+---------------------------+"
echo -e "| 1 - LED & Button Test     | 1D - 1, with remote debug |"
echo -e "| 2 - LED Test              | 2D - 2, with remote debug |"
echo -e "| 3 - LED With Button Test  | 3D - 3, with remote debug |"
echo -e "+---------------------------+---------------------------+"
echo -e "| Q - Quit                                              |"
echo -e "+---------------------------+---------------------------+"
echo -en "You choose: "
read USER_CHOICE
JAVA_CLASS=
REMOTE_DEBUG_FLAGS=
#
case "${USER_CHOICE}" in
  "1")
    JAVA_CLASS=diozerotests.FirstTest
    ;;
  "1D" | "1d")
    JAVA_CLASS=diozerotests.FirstTest
    REMOTE_DEBUG_FLAGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
    ;;
  "2")
    JAVA_CLASS=diozerotests.TestToggleLed
    ;;
  "2D" | "2d")
    JAVA_CLASS=diozerotests.TestToggleLed
    REMOTE_DEBUG_FLAGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
    ;;
  "3")
    JAVA_CLASS=diozerotests.TestPushButton
    ;;
  "3D" | "3d")
    JAVA_CLASS=diozerotests.TestPushButton
    REMOTE_DEBUG_FLAGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
    ;;
  "Q" | "q" | *)
    exit
    ;;
esac
#
# Make sure you have suspend=y below, for this kind of app.
# suspend=y will wait for the debugger to connect before moving on.
# REMOTE_DEBUG_FLAGS="${REMOTE_DEBUG_FLAGS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
if [[ "${REMOTE_DEBUG_FLAGS}" != "" ]]; then
  echo -e "Will use remote debug this parameters: ${REMOTE_DEBUG_FLAGS}"
fi
java -cp ${CP} ${REMOTE_DEBUG_FLAGS} ${JAVA_CLASS} $*
