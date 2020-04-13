# RMI in Java
Communication between Raspberry Pi and other machines (...like Raspberry Pi) using Remote Method Invocation (RMI).

See [this](https://docs.oracle.com/javase/tutorial/rmi/overview.html).

## RMI on RPi
We present here a very simple example of an RMI implementation **_with no security_**.

The `rmiregistry` is started from the server code itself, to make things easier.

- The `RemoteInterface` is defined in `compute.Compute`
- The server is `engine.ComputeEngine`
- The client is `client.ComputePi`

### Compile the code
```
$ ./compile
Interface Jars
added manifest
adding: compute/Compute.class(in = 307) (out= 201)(deflated 34%)
adding: compute/Task.class(in = 217) (out= 149)(deflated 31%)
Server classes
Client classes
$
```

### Start the server
_Important_: Modify the script named `start.server`, set the variable named `java.rmi.server.hostname`:
```
JAVA_OPTS="$JAVA_OPTS -Djava.rmi.server.hostname=RPiZero.att.net"
```
An IP address works fine too.

```
$ ./start.server &
Server address : RPiZero/127.0.0.1, port 1099

```

### Start the client

The second parameter to send to the script is the value set above, in `java.rmi.server.hostname`.
#### Linux and Mac
```
$ ./start.pi.client RPiZero 1099 50
 Executing java -cp .:./build/libs/compute.jar:./build/classes client.ComputePi
 Looking up [Compute on RPiZero.att.net:1099]
 3.14159265358979323846264338327950288419716939937511
$
```

#### Windows
```
Win> client.pi RPiZero 1099 50
 Looking up [Compute on RPiZero:1099]
 3.14159265358979323846264338327950288419716939937511
Win>
```

Something showed up in the server console:
```
 Server Computing PI with 50 decimals
```

Done!

### Further
There are also some other examples, involving `espeak`.
You can send messages (texts) to the server, and `espeak` will read them loud for you. (Yes, you need speakers connected on the RPi...)

Imagine coupling that with the `FONA`, when a message is received, it is read ;)

On the server (the Raspberry Pi):
```
 $ sudo apt-get install espeak
```
Then compile and start the server just like before

From the client:
#### Linux and Mac
```
$ ./start.speak.client RPiZero 1099 "The Raspberry Pi can speak remotely"
 Executing java -cp .:./build/libs/compute.jar:./build/classes client.AskToSpeak
 Looking up [Compute on RPiZero:1099]
 ...
$
```

#### Windows
```
Win> client.speak RPiZero 1099 "The Raspberry Pi can speak remotely"
 Looking up [Compute on RPiZero:1099]
 ...
Win>
```

