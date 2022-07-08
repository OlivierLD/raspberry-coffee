#!/bin/bash
CP=./build/libs/I2C-SPI-1.0-all.jar
# see system property ws.uri, default is ws://localhost:9876/
#
echo -e "Requires a WebSocket server running"
echo -e "Can be started with 'node server.js'"
sudo java -cp ${CP} i2c.samples.ws.WebSocketListener
