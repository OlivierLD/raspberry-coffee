#!/usr/bin/env bash
#
# 0. Cleanup
if [ -d "analogwatch" ]
then
  rm -rf analogwatch
fi
if [ -d "calendar" ]
then
  rm -rf calendar
fi
if [ -d "worldmap" ]
then
  rm -rf worldmap
fi
# 1. Build the required components
# We will need the world map, a watch and a calendar
#
HOME=$PWD
cd ../oliv-components/widgets/worldmap
echo -e "-------------------------"
echo -e "Building the WorldMap"
echo -e "-------------------------"
yarn
yarn build
cd ../analogwatch
echo -e "-------------------------"
echo -e "Building the Analog Watch"
echo -e "-------------------------"
yarn
yarn build
cd ../calendar
echo -e "-------------------------"
echo -e "Building the Calendar"
echo -e "-------------------------"
yarn
yarn build
# 2. Copy them in the production app folder
cd $HOME
echo -e "Back in $HOME, copying bundled resources"
echo -e "- Watch"
mkdir analogwatch
cp -r ../oliv-components/lib/analogwatch/*.min.* analogwatch
mkdir worldmap
cp -r ../oliv-components/lib/worldmap/*.min.* worldmap
mkdir calendar
cp -r ../oliv-components/lib/calendar/*.min.* calendar
# 3. How to refer to them
cat prod.index.html
# 4. Run
echo -e "When the server is started, reach http://localhost:8080/prod.samples/prod.index.html in a browser"
node ../server.js --wdir:../
