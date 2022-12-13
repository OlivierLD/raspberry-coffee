# Java to Python, Python to Java, using TCP

For a Java-to-Python communication, _**TCP**_ could be an option, it is socket-based, and natively supports read and write.

We would have the Java code acting as a TCP server, and we would wrap the Python code into
a TCP client.

The Java-TCP-server would:
- Start and initiate the TCP server, knowing its own IP address and port
- Start the python code - providing it IP address and port
  - The python code would then connect to the Java-TCP server, and use the sensor (or actuator) data to send to the TCP server.

This would require the maintenance of two kinds of code (Java and Python), but this should not be too difficult,
the Python TCP wrapper should not be a big deal. And on top of that, it could be re-used from any language knowing how to deal with TCP.

Let's see if we can come up with some scaffolding for this structure.

## Examples
As opposed to HTTP, TCP is a connected protocol.  
A _single_ HTTP request works like this: 
- connect to the server
- make a request
- get the response
- disconnect from the server  

Every HTTP request generates a new connection-disconnection.

A TCP client would connect to the server; once connected, it can make requests, and/or get responses.
And this until the client explicitly disconnects.  
See [here](https://realpython.com/python-sockets/#tcp-sockets), good Real-Python document.

> Theorically, a TCP server should be able to manage several clients. This is probably a good thing, but it might not always be a requirement here. 
> But we'll need to be careful.

### All Java (Java Server, Java Client(s))
This is a simple client-server duo. They both exchange _lines_ (finished with a NL).

- First, build the project
  - `../gradlew shadowJar`
  - This generates a `build/libs/Java-TCP-Python-1.0-all.jar`, which is quite small.
- Then, from a terminal, do a `./start.tcp.server.sh` to start the Java server
- And from one or more terminals, to start the client(s), do a `./start.tcp.client.sh`, it is an interactive program, you will be prompted.


### Java Server, Python Client(s)
Same server as above, start a TCP client in python:
- `python src/main/python/simple_tcp_client.py --port:5555`  

This is - as above - an interactive client. 

### Python Server, Java Client(s)
Look into the `python/nmea` folder for the servers. 

#### 2-way communication, Java client for ZDA_TCP_server
Start the TCP_ZDA_server:
```
$ python src/main/python/nmea/TCP_ZDA_server.py 
```
Then start a Java Swing client:
```
$ ./start.tcp.swing.client.sh
```
This is a Java Swing UI, it continuously reads the data from the server, and
it can also send messages to the TCP server, to produce the NMEA String faster, or slower.

#### Java client Continuously reading a Python server (BMP180)
Start thw Python server:
```
$ python src/main/python/sensors/bmp180/TCP_BMP180_basic_server.py --machine-name:192.168.1.105
```
Start the Java client:
```
./start.continuous.tcp.client.sh --port:7001 --host:192.168.1.105
(tcp.clients.SimpleContinuousTCPClient) Port now set to 7001
(tcp.clients.SimpleContinuousTCPClient) Enter '.' at the prompt to stop. Any non-empty string otherwise.
From Server: {"temperature": 18.9, "pressure": 100311, "altitude": 84.84824091482058, "sea-level-pressure": 100308.0}
From Server: {"temperature": 18.9, "pressure": 100313, "altitude": 84.51250197210659, "sea-level-pressure": 100316.0}
From Server: {"temperature": 18.9, "pressure": 100314, "altitude": 84.34463656570887, "sea-level-pressure": 100313.0}
From Server: {"temperature": 18.9, "pressure": 100313, "altitude": 83.75712898271219, "sea-level-pressure": 100316.0}
From Server: {"temperature": 18.9, "pressure": 100311, "altitude": 85.26792983782477, "sea-level-pressure": 100307.0}
From Server: {"temperature": 18.9, "pressure": 100313, "altitude": 84.26070487871105, "sea-level-pressure": 100316.0}
^CCtrl-C Exiting !
```

#### Java client reading a Python 'on-demand' server (BMP180)
Start thw Python server:
```
$ python src/main/python/sensors/bmp180/TCP_BMP180_ondemand_server.py --machine-name:192.168.1.105
```
Start the Java client:
```
$ ./start.tcp.client.sh --port:7001 --host:192.168.1.105
(tcp.clients.SimpleTCPClient) Port now set to 7001
(tcp.clients.SimpleTCPClient) Enter '.' at the prompt to stop. Any non-empty string otherwise.
User Request > STATUS
Client sending message: STATUS
Server responded {"source": "/home/pi/repos/raspberry-coffee/java-python/Java-TCP-Python/src/main/python/sensors/bmp180/TCP_BMP180_ondemand_server.py", "connected-clients": 1, "python-version": "3.9.2", "system-utc-time": "2022-12-12T13:18:37.000Z"}
User Request > GET_BMP180
Client sending message: GET_BMP180
Server responded {"temperature": 19.1, "pressure": 100091, "altitude": 103.0783456575366, "sea-level-pressure": 100088.0}
User Request > whatever
Client sending message: whatever
Server responded UN-MANAGED
User Request > GET_BMP180
Client sending message: GET_BMP180
Server responded {"temperature": 19.1, "pressure": 100080, "altitude": 103.4987711805293, "sea-level-pressure": 100082.0}
User Request > .
(tcp.clients.SimpleTCPClient) Client exiting
$
```

### Python Server, Python Client(s)
Shows how a two-way communication can work, asynchronously.  
Start the `TCP_ZDA_server`:
```
$ python src/main/python/nmea/TCP_ZDA_server.py 
```
Start the `simple_tcp_client.py`:
```
$ python src/main/python/simple_tcp_client.py --port:7001
```
This is an interactive client. Enter (in the terminal) "`faster`" or "`slower`", to change the string production rate.

## Pros and Cons
For the option that considers 
- Python server
- Java client(s)

### Pros
- TCP is language agnostic
- TCP is a _connected_ protocol
- If Python is doing the job, let it do the job
  - The breakout board provider usually provides the (Python) code that allows to put it to work
- Writing the Java driver implies mimicking what Python is doing. Talking to Python is a way to avoid this kind of duplication.

### Cons
- We need to agree of the payload (JSON is indeed an option)
- Wrapping the Python code into a TCP server is not trivial, but the structure of such code can be done generically.

### Conclusion ?
Sounds like a good option.

---
