#!/usr/bin/env bash
PROPS=
# PROPS="${PROPS} -Dstatic.docs=/web/"
PROPS="${PROPS} -Dsnap.verbose=true"
PROPS="${PROPS} -Dimage.rest.verbose=true"
# PROPS="${PROPS} -Dhttp.port=1234"
# PROPS="${PROPS} -Dwith.opencv=true"
# PROPS="${PROPS} -Dhttp.verbose=true"
# PROPS="${PROPS} -Dhttp.verbose.dump=true"
PROPS="${PROPS} -Dsnapshot.command=FSWEBCAM"
PROPS="${PROPS} -Dadditional.arguments.1=--device"
PROPS="${PROPS} -Dadditional.arguments.2=/dev/video1"
PROPS="${PROPS} -Dtime.based.snap.name=true"
#
OPENCV_HOME=/usr/local/share/java/opencv4
#
IP=$(hostname -I | awk '{ print $1 }')
echo -e "Make sure you've started the snap thread (in the REST server)! Port: default is 1234, override with -Dhttp.port"
echo -e "like curl -X POST http://${IP}:1234/snap/commands/start -H \"camera-rot: 0\" -H \"camera-width: 1280\" -H \"camera-height: 720\""
echo -e "Full operation list available from curl -X GET http://${IP}:1234/oplist"
echo -e "Snapshot from a browser http://${IP}:1234/web/index.html"
#
echo -en ">> Hit [return] to move on > "
read a
#
COMMAND="java ${PROPS} -Djava.library.path=${OPENCV_HOME} -jar build/libs/WebcamTemplate-1.0-all.jar"
echo -e "Running ${COMMAND}"
${COMMAND}
