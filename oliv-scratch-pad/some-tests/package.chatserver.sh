#!/bin/bash
FROM_DIR=$(pwd)
echo -e "Packaging the server..."
rm -rf classes
rm -rf dist
javac -d classes -s src/main/java \
        src/main/java/oliv/events/ServerInterface.java \
        src/main/java/oliv/events/ChatTCPServer.java \
        src/main/java/oliv/events/server.java 
mkdir dist
echo "Main-Class: oliv.events.server" > manifest.txt
cd classes
jar -cfm ../dist/server.jar ../manifest.txt *
#
echo -e "To run the server:"
echo -e "cd ../dist"
echo -e "java -jar server.jar --server-verbose:false --server-port:8000"
#
cd ${FROM_DIR}
echo -e "Packaging the client..."
# rm -rf classes
# rm -rf dist
javac -cp ./dist/server.jar \
        -d classes -s src/main/java \
        src/main/java/oliv/events/ChatTCPClient.java \
        src/main/java/oliv/events/client.java 
# mkdir dist
echo "Main-Class: oliv.events.client" > manifest.txt
cd classes
jar -cfm ../dist/client.jar ../manifest.txt *
#
echo -e "To run the client:"
echo -e "cd ../dist"
echo -e "java -jar client.jar --client-name:XXX --server-port:8000 --server-name:localhost"
#
cd ${FROM_DIR}
rm manifest.txt
echo -e "Done."
