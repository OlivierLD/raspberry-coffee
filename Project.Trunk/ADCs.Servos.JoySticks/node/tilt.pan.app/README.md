Requires the tilt-pan device (2 servos) to be connected, and the servo driver to be running, and lisrtening to the same WebSocket server.

To run once, to install the WebSocket module, from the `node` folder:
```
 $ npm install
```

Start the node server from the `node` folder:
```
 $ node server.js
```

Then start
```
 $ cd scripts
 $ pantilt.ws.sh
```

Then from a browser, reach http://localhost:9876/data/tilt.pan.app/servo.pilot.html
