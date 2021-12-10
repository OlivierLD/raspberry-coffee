# A Note about WebSockets

WebSocket is a protocol very similar to TCP, in that sense that it is connected (as opposed to HTTP,
which also uses TCP, but connects, executes, and disconnects, at each request), and remains connected
until the client explicitly requests a disconnection.

Once connected to a WebSocket server, a WebSocket client can
- push data to the WebSocket server
- receive data pushed _by_ the WebSocket server

The cool thing about WebSockets is that it is supported natively in all modern browsers.
A modern browser _**is**_ a potential WebSocket client.
As such, it can "push data to" and/or "receive data from" a WebSocket server.

It is the job of the server logic/implementation to decide what to do when a message is pushed to the
WebSocket server (rebroadcast to all other clients, to a specific client, log it, etc).

WebSocket server implementation are available in many languages, including Java and NodeJS.

### Example, use-case
The NMEA-Multiplexer can act as a REST or HTTP Server, but this is not mandatory.  
You could very well:
- Have _no_ REST or HTTP server enabled at the NMEA-Multiplexer level
- Read a Serial NMEA input from the NMEA-Multiplexer
- Push the NMEA Data to a WebSocket server, so every connected WebSocket client can get them.

#### Try it for yourself
We will use a NodeJS WebSocket server. Make sure `NodeJS` is installed.
```
$ cd NMEA-multiplexer
$ npm install    # To do once.
$ npm start
```
This should start the WebSocket/HTTP server.  
You could reach a client page from a web-socket savvy browser, at <http://localhost:9876/data/web/wsconsole.html>.

Then, from another console, in the same directory, after a build (`../gradlew shadowJar`)
```
$ ./mux.sh log.to.ws.yaml 
```
This reads a log file, and pushes the data to the WebSocket server.    
And those data should be received from the web page at <http://localhost:9876/data/web/wsconsole.html>.

Notice in [`log.to.ws.yaml`](./log.to.ws.yaml) that the HTTP Server is not enabled.
The `NMEA-multiplexer` just behaves as a pipeline.

---
