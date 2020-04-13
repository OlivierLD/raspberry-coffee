#!/usr/bin/env bash
#
# Build a Minimal NMEA Multiplexer.
#
# Add testing tool for the GPS
cp ../../NMEA.multiplexer/nmea.test.sh
#
mkdir web
cp ../../NMEA.multiplexer/web/index.min.html web/index.html
cp ../../NMEA.multiplexer/web/admin.html web/
cp ../../NMEA.multiplexer/web/logMgmt.html web/
cp ../../NMEA.multiplexer/web/runner.html web/
cp ../../NMEA.multiplexer/web/googlemaps.driving.html web/
cp ../../NMEA.multiplexer/web/console.html web/
cp ../../NMEA.multiplexer/web/pan*.gif web/
cp ../../NMEA.multiplexer/web/*.png web/
cp ../../NMEA.multiplexer/web/favicon.ico web/
#
cp -R ../../NMEA.multiplexer/web/css ./web
cp -R ../../NMEA.multiplexer/web/fonts ./web
cp -R ../../NMEA.multiplexer/web/icons ./web
cp -R ../../NMEA.multiplexer/web/images ./web
cp -R ../../NMEA.multiplexer/web/js ./web
cp -R ../../NMEA.multiplexer/web/widgets ./web
#
cd web
zip -r ../web.zip *
cd ..
rm -rf web
#
./to.prod.sh
rm web.zip
#
