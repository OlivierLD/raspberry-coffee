#!/bin/bash
echo -e "Initializing a new DB, get ready."
echo -en "Where should it be located [full path from here: ${PWD} ] : "
read DB_LOCATION
if [[ -f ${DB_LOCATION} ]]
then
  echo -e "This file already exists. Do you really want to proceed [y|n] ? > "
  read REPLY
  if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]
  then
    # You sure, hey?
    echo "Canceled."
    exit 0
  fi
fi
sqlite3 ${DB_LOCATION} < sql/image_db.sql
#
echo -e "Unless you've seen anything wrong, your ${DB_LOCATION} is ready for duty!"
ls -lisah ${DB_LOCATION}
