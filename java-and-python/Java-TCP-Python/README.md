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
An HTTP request works like this: 
- connect to the server
- make a request
- get the response
- disconnect from the server

A TCP client would connect to the server; once connected, it can make requests, and/or get responses.
And this until the client explicitly disconnects.

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
Look into the `python/nmea` folder.  


---
