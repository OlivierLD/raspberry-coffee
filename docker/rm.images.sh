#!/bin/bash
echo -e "------ Your Docker Images ------"
docker images
echo -e "--------------------------------"
echo -en "Are you sure you want to remove all of them? [n]|y|p (p: prompted) > "
read a
ONE_BY_ONE=false
if [ "$a" == 'Y' ] || [ "$a" == 'y' ] || [ "$a" == 'P' ] || [ "$a" == 'p' ]
then
  if [ "$a" == 'P' ] || [ "$a" == 'p' ]
  then
    ONE_BY_ONE=true
  fi
  echo -en "Ok, removing"
  if [ "$ONE_BY_ONE" == "true" ]
  then
    echo -en " one by one"
  fi
  echo -e "..."
else
  echo -e "Aborting"
  exit 1
fi
docker images | grep -v IMAGE | awk '{ print $3 }' > images
for iid in `cat images`
do
  if [ "$ONE_BY_ONE" == "true" ]
  then
    IMG_NAME=`docker images | grep $iid | awk '{ print $1 ":" $2 }' `
    echo -en ">> Remove image $IMG_NAME ($iid) [n]|y > "
    read resp
	if [ "$resp" == 'Y' ] || [ "$resp" == 'y' ]
	then
	  echo -e "... Removing $iid"
	  docker rmi $iid
	else
	  echo -e "Leaving $IMG_NAME alone"
	fi
  else
    docker rmi $iid
  fi
done
rm images > /dev/null
echo Done.
