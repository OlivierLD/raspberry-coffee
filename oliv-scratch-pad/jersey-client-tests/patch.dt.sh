#!/bin/bash
#
CP=./build/libs/jersey-client-tests-1.0-all.jar
#
java -cp ${CP} oliv.oda.PatchDecisionTable $*
