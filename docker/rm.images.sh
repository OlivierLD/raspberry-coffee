#!/bin/bash
echo Removing images
echo -e "--- Your Docker Images ---"
docker images
echo -en " Are you sure you want to remove all of them? [n]|y > "
read a
if [ "$a" == 'Y' ] || [ "$a" == 'y' ]
then
  echo -e "Ok, removing..."
else
  echo -e "Aborting"
  exit 1
fi
docker images | grep -v IMAGE | awk '{ print $3 }' > images
for iid in `cat images`
do
  docker rmi $iid
done
echo Done.
