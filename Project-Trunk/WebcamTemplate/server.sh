#!/usr/bin/env bash
PROPS=
# PROPS="${PROPS} -Dstatic.docs=/web/"
# PROPS="${PROPS} -Dsnap.verbose=true"
# PROPS="${PROPS} -Dimage.rest.verbose=true"
# PROPS="${PROPS} -Dhttp.port=1234"
PROPS="${PROPS} -Dwith.opencv=true"
# PROPS="${PROPS} -Dhttp.verbose=true"
# PROPS="${PROPS} -Dhttp.verbose.dump=true"
PROPS="${PROPS} -Dsnapshot.command=FSWEBCAM"
#
OPENCV_HOME=/usr/local/share/java/opencv4
#
IP=$(hostname -I | awk '{ print $1 }')
echo -e "Make sure you've started the snap thread (in the REST server)! Port: default is 1234, override with -Dhttp.port"
echo -e "like curl -X POST http://${IP}:1234/snap/commands/start -H \"camera-rot: 0\" -H \"camera-width: 480\" -H \"camera-height: 320\""
echo -e "Full operation list available from curl -X GET http://${IP}:1234/oplist"
#
echo -en "Hit return to move on "
read a
#
java ${PROPS} -Djava.library.path=${OPENCV_HOME} -jar build/libs/WebcamTemplate-1.0-all.jar
