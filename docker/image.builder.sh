#!/bin/bash
#
# Build and run a docker image
#
OK=false
DOCKER_FILE=
IMAGE_NAME=
RUN_CMD=
MESSAGE="Bye!"
#
while [ "$OK" = "false" ]
do
  # Menu
  echo -e "+-------------- D O C K E R  I M A G E  B U I L D E R ----------------+"
  echo -e "| 1. Nav Server, Debian                                               |"
  echo -e "| 2. Web Components, Debian                                           |"
  echo -e "| 3. To run on a Raspberry PI, Java, Raspberry Coffee, Web Components |"
  echo -e "| 4. Node PI, to run on a Raspberry PI                                |"
  echo -e "| 5. Node PI, to run on Debian                                        |"
  echo -e "| Q. Oops, nothing, thanks.                                           |"
  echo -e "+---------------------------------------------------------------------+"
  echo -en "== You choose => "
  read a
  #
  case "$a" in
    "Q" | "q")
      OK=true
      ;;
    "1")
      OK=true
      DOCKER_FILE=Dockerfile.navserver
      IMAGE_NAME=oliv-nav
			RUN_CMD="docker run -p 8081:8080 -d $IMAGE_NAME:latest"
      MESSAGE="Reach http://localhost:8081/oliv-components/index.html from your browser."
      ;;
    "2")
      OK=true
      DOCKER_FILE=Dockerfile.webcomponents
      IMAGE_NAME=oliv-webcomp
			RUN_CMD="docker run -p 9999:9999 -d $IMAGE_NAME:latest"
      MESSAGE="Reach http://localhost:9999/index.html from your browser."
      ;;
    "3")
      OK=true
      DOCKER_FILE=Dockerfile.rpi
      IMAGE_NAME=oliv-rpi
			RUN_CMD="docker run -p 8081:8080 -d $IMAGE_NAME:latest"
      MESSAGE="Reach http://localhost:8081/oliv-components/index.html from your browser."
      ;;
    "4")
      OK=true
      DOCKER_FILE=Dockerfile.node-pi
      IMAGE_NAME=oliv-nodepi
			# RUN_CMD="docker run -p 9876:9876 -t -i --device=/dev/ttyUSB0 $IMAGE_NAME:latest /bin/bash"
			RUN_CMD="docker run -p 9876:9876 -t -i --privileged -v /dev/ttyUSB0:/dev/ttyUSB0 -d $IMAGE_NAME:latest"
      # MESSAGE="See doc at https://github.com/OlivierLD/node.pi/blob/master/README.md"
      MESSAGE="Reach http://localhost:9876/data/demos/gps.demo.html in your browser"
      ;;
    "5")
      OK=true
      DOCKER_FILE=Dockerfile.node-debian
      IMAGE_NAME=oliv-nodedebian
			# RUN_CMD="docker run -p 9876:9876 --privileged -v /dev/tty.usbserial:/dev/ttyUSB0 -d $IMAGE_NAME:latest"
			RUN_CMD="docker run -p 9876:9876 -d $IMAGE_NAME:latest"
			IP_ADDR=`ifconfig | grep 'inet ' | grep -v '127.0.0.1' | awk '{ print $2 }'`
			if [ "$IP_ADDR" = "" ]
			then
			  IP_ADDR="localhost"
			fi
      MESSAGE="Reach http://$IP_ADDR:9876/data/demos/gps.demo.html in your browser"
      ;;
    *)
      echo -e "What? Unknown command [$a]"
      ;;
  esac
  #
done
#
#
if [ "$DOCKER_FILE" != "" ]
then
  cp $DOCKER_FILE Dockerfile
  #
  # Proxies,if needed
  # export HTTP_PROXY=http://www-proxy.us.oracle.com:80
  # export HTTPS_PROXY=http://www-proxy.us.oracle.com:80
  #
  echo -e "Generating $IMAGE_NAME"
  #
  docker build -t $IMAGE_NAME .
  #
  # Now run
  $RUN_CMD
fi
echo $MESSAGE
