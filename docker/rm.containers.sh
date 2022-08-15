#!/bin/bash
echo Stopping running containers
docker ps | grep -v CONTAINER | awk '{ print $1 }' > containers
for cid in `cat containers` ; do
  docker stop $cid
done
#
echo Removing containers
docker ps -a | grep -v CONTAINER | awk '{ print $1 }' > containers
for cid in `cat containers` ; do
  docker rm $cid
done
rm containers > /dev/null
echo Done.

#
# Also try this:
# docker stop $(docker ps -aq)
# docker rm $(docker ps -aq)
