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
ls -lish $REPO_ROOT | awk '{ print $11 " (size:" $7 ", created " $8 " " $9 " " $10 ")" }' > artifacts.txt
NL=0
while read -r line; do 
  if [[ ${NL} -gt 0 ]]; then
    echo -e "${NL} - ${line}"
  fi
  NL=$(expr ${NL} + 1)
done < artifacts.txt
#
echo -en "Enter the ARTIFACT line number > "
read LINE_NO
ARTIFACT=
if [[ "${LINE_NO}" != "" ]]; then
  NL=0
  while read -r line; do 
    if [[ "${NL}" == "${LINE_NO}" ]]; then
      ARTIFACT=$(echo ${line} | awk '{ print $1 }')
      echo -e "Select artifact ${ARTIFACT}"
      break
    fi
    NL=$(expr ${NL} + 1)
  done < artifacts.txt
fi
rm artifacts.txt
#
echo -e "Available versions:"
for VERS in $(find ${REPO_ROOT}/${ARTIFACT}/* -type d -maxdepth 1 -exec basename {} \;); do
  echo -e "- ${VERS}"
done	
echo -en "Enter the VERSION (default is ${VERSION}) > "
read a
if [[ "${a}" != "" ]]; then
  VERSION=${a}
fi
#
JAR_FILE_NAME=${HOME}/.m2/repository/oliv/raspi/coffee/${ARTIFACT}/${VERSION}/${ARTIFACT}-${VERSION}.jar
DATE_MODIFIED=$(ls -lisah ${JAR_FILE_NAME} | awk '{ print $8 " " $9 " " $10 }')
echo -e "${ARTIFACT} version ${VERSION}, was modified ${DATE_MODIFIED}"
echo -en "Do we proceed ? > "
read REPLY
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  echo "Canceled."
  exit 0
fi

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
  echo -e "+---------------------------------------------------------------+"
  echo -e "| Do not forget to commit and push the changes in this branch!! |"
  echo -e "+---------------------------------------------------------------+"
  git status
fi
#
