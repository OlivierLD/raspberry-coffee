#!/usr/bin/env bash
rm -rf web
rm web.zip
cp -R ../../RESTNavServer/launchers/web .
cd web
zip -r ../web.zip * -x 2019/**\*
cd ..
