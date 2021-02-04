#!/bin/bash
GROUP=oliv.raspi.coffee
ARTIFACT=http-tiny-server
VERSION=1.0
#
echo -en "Enter the GROUP (default is ${GROUP}) > "
read a
if [[ "${a}" != "" ]]
then
  GROUP=${a}
fi
#
echo -en "Enter the ARTIFACT (default is ${ARTIFACT}) > "
read a
if [[ "${a}" != "" ]]
then
  ARTIFACT=${a}
fi
#
echo -en "Enter the VERSION (default is ${VERSION}) > "
read a
if [[ "${a}" != "" ]]
then
  VERSION=${a}
fi
#
COMMAND="mvn install:install-file \
      -DgroupId=${GROUP} \
      -DartifactId=${ARTIFACT} \
      -Dversion=${VERSION} \
      -Dfile=${HOME}/.m2/repository/oliv/raspi/coffee/${ARTIFACT}/${VERSION}/${ARTIFACT}-${VERSION}.jar \
      -Dpackaging=jar \
      -DgeneratePom=true \
      -DlocalRepositoryPath=. \
      -DcreateChecksum=true"
#
echo -e "Command is:"
echo -e "${COMMAND}"
echo -en "Do we proceed ? > "
read REPLY
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]
then
  echo "Canceled."
  exit 0
else
  ${COMMAND}
fi
#
echo -e "Done"

