#!/bin/bash
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
echo -e "🌟 Distrib generated in ../lib/$CompDir"
# echo -e "👉>> From $PWD"
#cat ../../publish.utils/packagejson.part.01.txt > ../lib/${CompDir}/package.json
#echo -e "  \"name\": \"$CompDir\"," >> ../lib/${CompDir}/package.json
#cat ../../publish.utils/packagejson.part.02.txt >> ../lib/${CompDir}/package.json
echo -e "🚚 $CompDir ready to ship"