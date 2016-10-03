# RMI in Java
See [this](https://docs.oracle.com/javase/tutorial/rmi/overview.html).

## RMI on RPi
We present here a very simple example of an RMI implementation **_with no security_**.

The `rmiregistry` is started from the server code itself, to make things easier.

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
```
$ ./start.server &
Server address : oliv-machine/127.0.0.1, port 1099

``` 
 
### Start the client 
```
$ ./start.client oliv-machine 1099 50
 Executing java -cp .:./build/libs/compute.jar:./build/classes client.ComputePi
 Looking up [Compute on oliv-machine:1099]
 3.14159265358979323846264338327950288419716939937511
$
``` 

Something showed up in the server console:
```
  Server Computing PI with 50 decimals

```

