#!/bin/bash
#
# Build and run a docker image
#
cp Dockerfile.webcomponents Dockerfile
# cp Dockerfile.navserver Dockerfile
#
# Proxies,if needed
# export HTTP_PROXY=http://www-proxy.us.oracle.com:80
# export HTTPS_PROXY=http://www-proxy.us.oracle.com:80
#
IMAGE_NAME=oliv-image
# IMAGE_NAME=oliv-nav
#
docker build -t $IMAGE_NAME .
#
# Now run
docker run -p 8081:8080 -d $IMAGE_NAME:latest
echo Reach http://localhost:8081/oliv-components/index.html from your browser.
