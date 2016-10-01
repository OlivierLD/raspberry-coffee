# RMI in Java
Communication between Raspberry PI and other machines (...like Raspberry PI) using Remote Method Invocation (RMI).
 
See [this](https://docs.oracle.com/javase/tutorial/rmi/overview.html).

## RMI on RPi
We present here a very simple example of an RMI implementation **_with no security_**.

The `rmiregistry` is started from the server code itself, to make things easier.
 
## Run the example
It really works :)

On the Raspberry PI:
```
# From the RMI.sample directory
$ ./compile
@ ./start/client
```

From another computer (in this case, a Windows laptop. Yes, Windows), from the RMI.sample directory:
```
C:\WhereYouAre> compile
... 
C:\WhereYouAre> client raspberrypi3.att.net Compute 55
Looking up [Compute]
3.1415926535897932384626433832795028841971693993751058210

C:\WhereYouAre>
```

## How it works
Aha!
