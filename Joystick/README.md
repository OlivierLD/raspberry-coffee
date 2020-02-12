## WiP: Using a Joystick
The challenge here is to read one or more Joystick ports, and to render their positions
in a web page.

Reading the data emitted by the joystick can be done from Java or Python (and many other languages of course,
we'll stick to those two here).

Pushing those data to a web page can be achieved using WebSockets (which is a W3C standard).

So, we will have 3 distinct components here:
- A WebSocket server, implemented here on NodeJS
- A Java or Python component, reading the joystick data, and pushing them onto the WebSocket server every time they change. The server will then re-broadcast those data to all the WebSocket clients connected to it
- A Web page - WebSocket aware - that will receive the data re-broadcasted by the server. This web page can be served by the Node server implementing the WebSocket layer.

In theory, we could have several web pages connected to the WebSocket server, even if this is unlikely to happen.

### Install the NodeJS server part
To install NodeJS on the Raspberry Pi, see [here](https://www.w3schools.com/nodejs/nodejs_raspberrypi.asp), and [here](https://github.com/nodesource/distributions#debinstall).

From the directory where `package.json` is:
```
 $ npm install
```

### Build the Java part
From the same directory (where `build.gradle` is):
```
 $ ../gradlew shadowJar
```

### Start the NodeJS server
```
 $ node joystick.server.js
```

### Start the Java joystick driver
```
 $ ./joystick.sh
```

### Reach the Web UI
<http://[raspberry.address]:9876/web/index.html>


### Screenshots
![Center](./docimg/01.png)

![Bottom Right](./docimg/02.png)

## Python version
Requires a [WebSocket client](https://pypi.org/project/websocket_client/).
```
 $ pip install websocket_client
``` 
 
First early version (like Java, requires the Node server to be running):
```
 $ python src/main/python/sample_implementation.py
```

Also try things like this
```
 $ python -i src/main/python/joystick_reader.py
 >>> help(JoystickReader)
 . . .
 >>> execfile('src/main/python/sample_implementation.py')
 . . .
```

The the same Web UI should respond, like above.

---
