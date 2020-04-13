#!/usr/bin/env bash
#
# Build a full nav server with all the features
#
cp -R ../../RESTNavServer/launchers/web .
cd web
zip -r ../web.zip * -x 2019/**\*
cd ..
rm -rf web
#
./to.prod.sh NMEADist
rm web.zip
#
