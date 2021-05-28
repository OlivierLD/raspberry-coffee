#!/bin/bash
#
CP=./build/libs/jersey-client-tests-1.0-all.jar
#
java -cp ./build/libs/jersey-client-tests-1.0-all.jar -Dapp.name="ApprovalPOC" -Ddecision.table="Manager Preference" -Dverbose=false oliv.oda.ResetDecisionTable
#
java -cp ./build/libs/jersey-client-tests-1.0-all.jar -Dapp.name="ObiPOC" -Ddecision.table="Strategy" -Dverbose=false oliv.oda.ResetDecisionTable
