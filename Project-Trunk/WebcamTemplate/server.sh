#!/usr/bin/env bash
PROPS=
# PROPS="${PROPS} -Dstatic.docs=/web/"
# PROPS="${PROPS} -Dsnap.verbose=true"
# PROPS="${PROPS} -Dimage.rest.verbose=true"
PROPS="${PROPS} -Dhttp.port=1234"
# PROPS="${PROPS} -Dwith.opencv=true"
# PROPS="${PROPS} -Dhttp.verbose=true"
# PROPS="${PROPS} -Dhttp.verbose.dump=true"
PROPS="${PROPS} -Dsnapshot.command=FSWEBCAM"   # WebCam, on Raspberry Pi
PROPS="${PROPS} -Dadditional.arguments.1=--device"        # For fswebcam
PROPS="${PROPS} -Dadditional.arguments.2=/dev/video0"     # For fswebcam
# PROPS="${PROPS} -Dadditional.arguments.2=/dev/video1"
PROPS="${PROPS} -Dtime.based.snap.name=true"
#
START_SNAP_IMMEDIATELY=true
if [[ "${START_SNAP_IMMEDIATELY}" == "true" ]]
then
  PROPS="${PROPS} -Dstart.snap.thread=true"
  PROPS="${PROPS} -Dsnap.rot=0"
  PROPS="${PROPS} -Dsnap.width=640"
  PROPS="${PROPS} -Dsnap.height=360"
fi
#
# OPENCV_HOME=/usr/local/share/java/opencv4
OPENCV_HOME=/home/pi/opencv-4.5.2/build/lib
#
IP=$(hostname -I | awk '{ print $1 }')
if [[ "${IP}" == "" ]]
then
  IP=localhost
fi
echo -e "Port: default is 1234, override with -Dhttp.port"
if [[ "${START_SNAP_IMMEDIATELY}" == "true" ]]
then
  echo -e "Make sure you've started the snap thread (in the REST server)! "
  echo -e "like in:"
  echo -e "  curl -X POST http://${IP}:1234/snap/commands/start -H \"camera-rot: 0\" -H \"camera-width: 1280\" -H \"camera-height: 720\""
fi
echo -e "Full operation list available from curl -X GET http://${IP}:1234/oplist"
echo -e "To take snapshots from a browser: http://${IP}:1234/web/index.html"
#
echo -en ">> Hit [return] to move on... "
read a
#
COMMAND="java ${PROPS} -Djava.library.path=${OPENCV_HOME} -jar build/libs/WebcamTemplate-1.0-all.jar"
echo -e "Running ${COMMAND}"
${COMMAND}
