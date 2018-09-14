#!/usr/bin/env bash
#
# To generate a package.json, 'npm init' is an option
# To get your own local npm registry (https://www.npmjs.com/package/local-npm) :
# $ npm set registry https://registry.npmjs.org
# $ cd WebComponents
# $ npm install [-g] local-npm
# The local registry must be running:
# $ local-npm [&]
# Make sure you've set the registry to the local one:
# $ npm set registry http://127.0.0.1:5080
# To switch back to what it was :
# $ npm set registry https://registry.npmjs.org (or whatever it was)
#
# To browse the local registry:
#  http://localhost:5080/_browse
#
# WARNING: local-npm does not always work... version mismatch and such shit.
#
echo -e "+-------------------------+"
echo -e "+-- P U B L I S H I N G --+"
echo -e "+-------------------------+"
echo -e "| 1. AnalogDisplay        |"
echo -e "| 2. AnalogWatch          |"
echo -e "| 3. CalendarDisplay      |"
echo -e "| ...                     |"
echo -e "+-------------------------+"
echo -e "| Q to quit               |"
echo -e "+-------------------------+"
echo -en "- You choose > "
read response
#
npm config list
#
case "$response" in
  "1")
    cd analogdisplay
    npm publish . --dry-run
    # npm publish .
    echo "Done with AnalogDisplay"
    ;;
  "q" | "Q")
    ;;
   *)
    echo -e "What? Unknown or un-implemented command [$response]"
    ;;
esac
