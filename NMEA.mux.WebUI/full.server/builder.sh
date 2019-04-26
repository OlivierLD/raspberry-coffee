#!/usr/bin/env bash
#
# Build a full nav server with all the features
#
zip -r ./web.zip ../../RESTNavServer/launchers/web/* -x ../../RESTNavServer/launchers/web/2019/**\*
#
./to.prod.sh
#
