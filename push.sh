#!/bin/bash
REPO_ROOT=${HOME}/.m2/repository/oliv/raspi/coffee
#
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
echo -e "Available atifacts:"
for ART in `ls $REPO_ROOT`; do
  echo -e "- ${ART}"
done	
echo -en "Enter the ARTIFACT (default is ${ARTIFACT}) > "
read a
if [[ "${a}" != "" ]]; then
  ARTIFACT=${a}
fi
#
echo -e "Available versions:"
for VERS in $(find $REPO_ROOT/$ARTIFACT/* -type d -maxdepth 1 -exec basename {} \;); do
  echo -e "- ${VERS}"
done	
echo -en "Enter the VERSION (default is ${VERSION}) > "
read a
if [[ "${a}" != "" ]]; then
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
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  echo "Canceled."
  exit 0
else
  ${COMMAND}
  echo -e "Done"
  echo -e "+----------------------------------------------------+"
  echo -e "| Do not forget to push the changes in this branch!! |"
  echo -e "+----------------------------------------------------+"
  git status
fi
#
