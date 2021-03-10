#!/usr/bin/env bash
#
# Build a full nav server with all the features
#
cp -R ../../RESTNavServer/launchers/web .
# TODO Other folders (xsl, ub, libs) ?
cd web
# web.zip is the default value for property "web.archive"
zip -r ../web.zip * -x 20??/**\*
cd ..
rm -rf web
#
./to.prod.sh
rm web.zip
#
