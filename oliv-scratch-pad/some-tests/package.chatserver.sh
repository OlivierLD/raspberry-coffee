#!/bin/bash
FROM_DIR=$(pwd)
echo -e "Packaging the server..."
rm -rf classes
rm -rf dist
#
# No debug option (to keep it small)
# Add a '-g' to the javac command to have it.
#
javac -d classes -s src/main/java \
      src/main/java/oliv/tcp/chat/Utils.java \
      src/main/java/oliv/tcp/chat/ServerInterface.java \
      src/main/java/oliv/tcp/chat/ChatTCPServer.java \
      src/main/java/oliv/tcp/chat/Server.java
mkdir dist
echo "Main-Class: oliv.tcp.chat.Server" > manifest.txt
echo "Compile-date: $(date)" >> manifest.txt
cd classes
jar -cvfm ../dist/server.jar ../manifest.txt *
#
echo -e "To run the server:"
echo -e "cd ../dist"
echo -e "java -jar server.jar --server-verbose:false --server-port:8000"
echo -e "For help: java -jar server.jar --help"
#
cd ${FROM_DIR}
echo -e "Packaging the client..."
rm -rf classes
# rm -rf dist
javac -d classes -s src/main/java \
      src/main/java/oliv/tcp/chat/Utils.java \
      src/main/java/oliv/tcp/chat/ChatTCPClient.java \
      src/main/java/oliv/tcp/chat/Client.java
# mkdir dist
echo "Main-Class: oliv.tcp.chat.Client" > manifest.txt
echo "Compile-date: $(date)" >> manifest.txt
cd classes
jar -cvfm ../dist/client.jar ../manifest.txt *
#
echo -e "To run the client:"
echo -e "cd ../dist"
echo -e "java -jar client.jar --client-name:XXX --server-port:8000 --server-name:localhost --client-verbose:false"
echo -e "For help: java -jar client.jar --help"
echo -e "... for the speech, try 'java -Dspeak-french=true -jar client --client-speech:true' (french on Mac only)"
#
cd ${FROM_DIR}
rm manifest.txt
echo -e "Done."
#
echo -e "--- T O   D I S T R I B U T E   A N D   R U N ---"
echo -e "-------------------------------------------------"
echo -e "* To start a server:"
echo -e "  - get the server.jar from the dist folder"
echo -e "  - from the folder where it lives in, run the 'java -jar server.jar ... ' command."
echo -e "* To start a client:"
echo -e "  - get the client.jar from the dist folder"
echo -e "  - from the folder where it lives in, run the 'java -jar -client.jar ... ' command (once server is started)."
echo -e "-------------------------------------------------"
