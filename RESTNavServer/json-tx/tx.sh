#!/bin/bash
#
# Tentative JSON-to-JSON transformation using JQ.
#
# jq cheat sheet: https://lzone.de/cheat-sheet/jq
#
# jq '.Position | { "latitude": .lat, "longitude": .lng }' data-cache.json
# jq '.Position , ."Solar Time" | { "position": { "latitude": .lat, "longitude": .lng}, "solar-time": .fmtDate | .epoch }' data-cache.json
# jq '.Position , ."Solar Time" | select((.lat != null) and (.lng != null)) | "position": { "latitude": .lat, "longitude": .lng }, select(.fmtDate != null) | , "epoch": .fmtDate | .epoch }' data-cache.json
# jq '[.Position , ."Solar Time"] | { "position": { "latitude": .lat, "longitude": .lng}, "solar-time": .fmtDate | .epoch }' data-cache.json
# jq '[.Position , ."Solar Time"] | select((.lat != null) and (.lng != null)) | "position": { "latitude": .lat, "longitude": .lng }, select(.fmtDate != null) | [ "epoch", .fmtDate | .epoch ] | join(",")' data-cache.json
# jq '.Position | { "latitude": .metadata["latitude"], .lat }' data-cache.json
# jq '.Position | { .metadata["latitude"], .lat }' data-cache.json
#  Works:
# jq '[.Position , ."Solar Time"] | .[] | select((.lat != null) and (.lng != null)) | { "position": { "latitude": .lat, "longitude": .lng }}' data-cache.json
#
# jq '[.Position , ."Solar Time"] | .[] | select((.lat != null) and (.lng != null)) | { "position": { "latitude": .lat, "longitude": .lng }}, select(.fmtDate != null) | [ "epoch", .fmtDate | .epoch ]' data-cache.json
# Works:
# jq '[.Position , ."Solar Time"] | .[] | (select((.lat != null) and (.lng != null)) | { "position": { "latitude": .lat, "longitude": .lng }}), (select(.fmtDate != null) | { "epoch": .fmtDate.epoch })' data-cache.json
# Works:
# jq '.Position , ."Solar Time" | (select((.lat != null) and (.lng != null)) | { "position": { "latitude": .lat, "longitude": .lng }}), (select(.fmtDate != null) | { "epoch": .fmtDate.epoch })' data-cache.json
#
#
# Option 1
jq '.Position | { "latitude": .lat, "longitude": .lng }' data-cache.json > one.txt
jq '."Solar Time" | (.fmtDate | .epoch) ' data-cache.json > two.txt
jq '.NMEA ' data-cache.json > three.txt

echo -e "{ \"position\": $(cat one.txt), \"epoch\": $(cat two.txt), \"nmea\": $(cat three.txt) }"
#
if [[ 1 == 2 ]]; then
  # Option 2
  echo -e "{"
  echo -e "==================="
  jq '.Position | { metadata["position"], { "latitude": .lat, "longitude": .lng } }' data-cache.json
  echo -e "==================="
  jq '."Solar Time" | { metadata["created"], (.fmtDate | .epoch) }' data-cache.json
  echo -e "==================="
  echo -e "}"
fi
