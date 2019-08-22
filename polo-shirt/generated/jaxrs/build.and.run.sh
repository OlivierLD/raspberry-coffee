#!/usr/bin/env bash
echo -e "This script can be modified for its settings"
SETTINGS=
# SETTING="--settings ../../settings.xml"
echo -e "Settings: $SETTINGS"
#
echo -e "Try curl -X POST http://localhost:2345/top-root/greeting/v3 -H \"Content-Type: application/json\" -d '{\"name\": \"Olivier\", \"salutation\": \"Hi\" }' "
#
# -Dexec.args="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
# -Dexec.args="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
mvn clean package jetty:run $SETTINGS -Dmaven.test.skip=true
