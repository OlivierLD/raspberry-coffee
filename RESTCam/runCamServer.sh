#!/bin/bash
echo Running Cam Server
#
CP=./build/libs/RESTCam-1.0-all.jar
#
HTTP_PORT=9999
#
SERVO_HEADING=14
SERVO_TILT=15
#
echo -e "---------------------------"
echo -e "Tilt servo is #${SERVO_TILT}, Heading servo is #${SERVO_HEADING}"
echo -en "Is that OK y|[n] ? > "
read REPLY
if [[ "${REPLY}" == "" ]]
then
  REPLY=n
fi
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]
then
  # then prompt the user
  echo -en "Tilt servo # ? > "
  read SERVO_TILT
  echo -en "Heading servo # ? > "
  read SERVO_HEADING
	echo -e "Now, Tilt servo is #${SERVO_TILT}, Heading servo is #${SERVO_HEADING}"
	echo -e "---------------------------"
fi
#
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dcam.verbose=true"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dservo.heading=${SERVO_HEADING}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dservo.tilt=${SERVO_TILT}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.port=${HTTP_PORT}"
#
HOST=`hostname`
DOMAIN=`domainname`
if [[ "${DOMAIN}" != "" ]]
then
	HOST=${HOST}.${DOMAIN}
fi
URL=http://${HOST}:${HTTP_PORT}/web/test.html
#
IP_ADDR=`ifconfig wlan0 2> /dev/null  | awk '/inet addr:/ {print $2}' | sed 's/addr://'`
if [[ "${IP_ADDR}" != "" ]]
then
	echo Server IP is ${IP_ADDR}
fi
#
echo "Try opening ${URL} in a browser"
#
sudo java -cp ${CP} ${JAVA_OPTIONS} cam.CamServer
