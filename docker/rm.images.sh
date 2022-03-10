#!/bin/bash
#
# docker rmi takes options: --force, --no-prune
#
echo -e "------ Your Docker Images ------"
docker images
echo -e "--------------------------------"
echo -en "Are you sure you want to remove all of them? [n]|y|p|none (p: prompted, none: remove images tagged <none>) > "
read a
ONE_BY_ONE=false
NONE_ONLY=false
if [[ "$a" == 'Y' ]] || [[ "$a" == 'y' ]] || [[ "$a" == 'P' ]] || [[ "$a" == 'p' ]] || [[ "$a" == 'none' ]]
then
  if [[ "$a" == 'P' ]] || [[ "$a" == 'p' ]]
  then
    ONE_BY_ONE=true
  fi
  if [[ "$a" == 'none' ]]
  then
    NONE_ONLY=true
  fi
  echo -en "Ok, removing"
  if [[ "$ONE_BY_ONE" == "true" ]]
  then
    echo -en " one by one"
  fi
  echo -e "..."
else
  echo -e "Unknown command $a"
  echo -e "Aborting"
  exit 1
fi
if [[ "$NONE_ONLY" == "true" ]]
then
  docker images | grep '<none>' | awk '{ print $3 }' > images
  echo -e "Will delete the following images:"
  docker images | grep '<none>'
  echo -en "Proceed ? y|n > "
  read a
  if [[ "$a" != 'y' ]] && [[ "$a" != 'Y' ]]
  then
    echo -e "Cancelling..."
    rm images > /dev/null
    exit 0
  fi
else
  docker images | grep -v IMAGE | awk '{ print $3 }' > images
fi
for iid in `cat images`
do
  if [[ "$ONE_BY_ONE" == "true" ]]
  then
    IMG_NAME=`docker images | grep $iid | awk '{ print $1 ":" $2 }' `
    echo -en ">> Remove image $IMG_NAME ($iid) [n]|y > "
    read resp
		if [[ "$resp" == 'Y' ]] || [[ "$resp" == 'y' ]]
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

#
# Also try this:
#
# sudo su -
#
# docker stop $(docker ps -aq)
# docker rm $(docker ps -aq)
# docker rmi $(docker images -aq)
# docker volume prune
#
