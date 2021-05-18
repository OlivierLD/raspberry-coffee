#!/bin/bash
#
# Require jq
JQ_PRESENT=$(which jq)
if [[ "${JQ_PRESENT}" == "" ]]
then
  echo "+----------------------------------------+"
  echo "| The script requires jq to be available |"
  echo "| Use 'brew install jq' to install it    |"
  echo "+----------------------------------------+"
  exit 1
fi
#
INSTANCE=idcs-oda-d992cfbabc1744baab766fb7464b924c-s0.data.digitalassistant.oci.oc-test.com
#
USER_NAME=oda-ServiceAdministrator
PASSWORD=We1come12345*
TTL=3600
#
OPTION=two
#
if [[ "${OPTION}" == "one" ]]
then
     PROCESS_APP_NAME=OlivTestApp
     VERSION=1.0
     # WHERE=active
     WHERE=latest
     DECISION_SERVICE_NAME=AmountCheck
     DECISION_TABLE=BigEnough
elif [[ "${OPTION}" == "two" ]]
then
     PROCESS_APP_NAME=NL2Rules
     VERSION=1.0
     # WHERE=active
     WHERE=latest
     DECISION_SERVICE_NAME=ExpenseApprovalStrategy
     DECISION_TABLE=Manager%20Preference
else
     PROCESS_APP_NAME=ApprovalPOC
     VERSION=1.0
     # WHERE=active
     WHERE=latest
     DECISION_SERVICE_NAME=ApprovalStrategyService
     DECISION_TABLE=ApprovalStrategy
fi
#
echo "------------------------------------------"
echo "Getting access token"
echo "------------------------------------------"
ACCESS_TOKEN=$(curl -L -X POST 'https://idcs-8ec236754c304a6d9ff0afae76347b8e.identity.preprod.oraclecloud.com/oauth2/v1/token'   \
   -H 'Authorization: Basic aWRjcy1vZGEtZDk5MmNmYmFiYzE3NDRiYWFiNzY2ZmI3NDY0YjkyNGMtczBfQVBQSUQ6MDZiM2Y5YjAtMjI3YS00ODlhLWJlZjctZDBiMjk3NmMzYzBi' \
   -H 'Content-Type: application/x-www-form-urlencoded' \
   --data-raw "scope=https://${INSTANCE}/process&grant_type=password&username=${USER_NAME}&password=${PASSWORD}&expiry=${TTL}" | jq '.access_token')
 
TRIM_TOKEN=$(echo ${ACCESS_TOKEN} | sed -e 's/^"//' -e 's/"$//')

RESOURCE="https://${INSTANCE}/decision/api/v1/decision-models/${PROCESS_APP_NAME}/versions/${VERSION}/${WHERE}/definition/decisions/${DECISION_TABLE}"
#
# Get current state
#
echo "------------------------------------------"
echo "Getting the current DecisionTable state"
echo "------------------------------------------"
# add a --verbose if needed
curl -L \
     -X GET ${RESOURCE} -H "Authorization: Bearer ${TRIM_TOKEN}" > old.json
#
DT_NAME=$(cat old.json | jq '.name')
echo "------------------------------------------"
echo "Showing current amounts for ${DT_NAME}"
echo "------------------------------------------"
cat old.json | jq '.logic.references[].components[]'
echo "------------------------------------------"
# cat old.json | jq '.logic.rules[].inputEntries[] | .range, .value'
cat old.json | jq '.logic.rules[] | (.inputEntries[] | .range), (.outputEntries [] | .value)'
#
# Change amount, with sed (brute force).
#
FROM=200
TO=250
#
echo -en "Will change [${FROM}] to [${TO}], is that all right? > "
read REPLY
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]
then
     echo -en "Enter the amount to change (original) > "
     read FROM
     echo -en "Enter the new amount > "
     read TO
     echo -en "Will change [${FROM}] to [${TO}], is that all right? > "
     read REPLY
     if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]
     then
          echo "Canceled."
          exit 0
     fi
fi
#
echo "------------------------------------------"
echo -e "Updating from ${FROM} to ${TO}"
echo "------------------------------------------"
sed "s/${FROM}/${TO}/g" old.json > new.json
echo "- Done"
#
# Update Decision Table
#
echo "------------------------------------------"
echo "Updating the DecisionTable"
echo "------------------------------------------"
curl -L -X PUT ${RESOURCE} -H "Authorization: Bearer ${TRIM_TOKEN}" -H "Content-Type: application/json" --data @new.json
echo "- Done"
#
# Check
#
echo "------------------------------------------"
echo "After update:"
echo "------------------------------------------"
curl -L -X GET ${RESOURCE}  \
     -H "Content-Type: application/json"  \
     -H "Authorization: Bearer ${TRIM_TOKEN}" | jq '.logic.rules[].inputEntries[] | .range, .value'
#
echo -en "Do we proceed with activation? > "
read REPLY
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]
then
  echo "Exiting."
  exit 0
fi
#
echo "------------------------------------------"
echo "Activating"
echo "------------------------------------------"
curl -L --verbose -X PUT https://${INSTANCE}/process/api/v1/applications/${PROCESS_APP_NAME}/versions/${VERSION}/activate \
     -H "Authorization: Bearer ${TRIM_TOKEN}"
#    -H "Content-Type: application/json" 
#    --data-raw '{ "snapshotId": "xxxx" }'


