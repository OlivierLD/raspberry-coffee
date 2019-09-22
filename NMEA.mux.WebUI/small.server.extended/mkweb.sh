#!/usr/bin/env bash
rm -rf web
rm web.zip
cp -R ../../RESTNavServer/launchers/web .
cp -R ../../RESTNavServer/launchers/xsl .
cp -R ../../RESTNavServer/launchers/pub .
cp -R ../../RESTNavServer/launchers/libs .
cd web
zip -r ../web.zip * -x 2019/**\*
cd ..
