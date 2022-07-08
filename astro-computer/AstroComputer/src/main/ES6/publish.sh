#!/bin/bash
#
# Requires yarn (sudo npm install [--force] --global yarn)
#
echo -e "--- npm config ---"
npm config list
#
CompDir=$(pwd)
echo -e "👉 Deliverables in $CompDir"
cd ${CompDir}
#   npm publish . --dry-run
yarn
yarn build
# npm publish .
cd ..
echo -e "🌟 Distrib generated in $CompDir/lib"
# echo -e "👉>> From $PWD"
#cat ../../publish.utils/packagejson.part.01.txt > ../lib/${CompDir}/package.json
#echo -e "  \"name\": \"$CompDir\"," >> ../lib/${CompDir}/package.json
#cat ../../publish.utils/packagejson.part.02.txt >> ../lib/${CompDir}/package.json
echo -e "🚚 $CompDir/lib ready to ship"
echo -e "Change your import in app.js to use it."
