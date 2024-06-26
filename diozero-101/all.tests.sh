#!/bin/bash
CP=./build/libs/diozero-101-1.0-all.jar
#
echo -e "+-------------------------------------------------------+"
echo -e "|        D I O Z E R O   B A S I C   T E S T S          |"
echo -e "+---------------------------+---------------------------+"
echo -e "| 1 - LED & Button Test     | 1D - 1, with remote debug |"
echo -e "| 2 - LED Test              | 2D - 2, with remote debug |"
echo -e "| 3 - LED With Button Test  | 3D - 3, with remote debug |"
echo -e "| 4 - DOD Test (relay, led) | 4D - 4, with remote debug |"
echo -e "| 5 - MCP3008 Test          | 5D - 5, with remote debug |"
echo -e "| 6 - BMP180 Test (I2C)     | 6D - 6, with remote debug |"
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
  "4")
    JAVA_CLASS=diozerotests.TestDOD
    ;;
  "4D" | "4d")
    JAVA_CLASS=diozerotests.TestDOD
    REMOTE_DEBUG_FLAGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
    ;;
  "5")
    JAVA_CLASS=diozerotests.MCP3008
    ;;
  "5D" | "5d")
    JAVA_CLASS=diozerotests.MCP3008
    REMOTE_DEBUG_FLAGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
    ;;
  "6")
    JAVA_CLASS=diozerotests.MCP3008
    ;;
  "6D" | "6d")
    JAVA_CLASS=diozerotests.I2C_BMP180
    REMOTE_DEBUG_FLAGS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
    echo -e "Make sure you see address 77 when you do an 'i2cdetect -y 1'"
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
if [[ "$*" != "" ]]; then
  echo -e "Script parameters: $*"
fi
java -cp ${CP} ${REMOTE_DEBUG_FLAGS} ${JAVA_CLASS} $*
