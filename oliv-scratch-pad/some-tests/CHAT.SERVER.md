# TCP Based Java Chat Server
This is presented as a small example, it does not pretend to go to production.  
> The goal here is to show the scaffolding for such a server, while keeping the code as small as possible.  
> The features presented here are minimal, but this server _**is**_ working.

> We use here (for the _client_ part, server part should be OK) several statements that require Java 11 (`Map.of`, `Objects.requireNonNullElse`). It could be modified if a lower Java version is required (like for the Raspberry Pi Zero).

It provides the following basic features (which you can extend):
- Messages are all text messages
  - The `ChatTCPClient` is using a `in.readLine()` to read its messages, _that requires the messages sent to the client to end with a `NL` character_.  
  This feature would require to be changed for other types of messages (binaries, text not finishing with `NL`, etc). 
- When a client is sending a message, it is re-broadcasted to all _other_ connected clients.
- If the server is taken down, all still-connected clients are notified.

Look into the package `oliv.tcp.chat`.  
It can be compiled and distributed by using the script `package.chatserver.sh`.
Same script provides instructions on how to run the server and its client(s).
The script produces 2 jars in the `dist` sub-folder, just above ~10Kb big (they could be even smaller, there is a text-to-speech feature available, for example).

You will notice that an entry named `Compile-date` is added to the `MANIFEST.MF`.
Look in the code to see how it is used.

### Quick start:  
- From the folder it's in, run the script `package.chatserver.sh`.  
  This will generate a clean `dist` folder, containing the archives to distribute.

#### Server side
- Send the `server.jar` (newly generated in the `dist` folder) to the machine you want to run the server on.
    -  `scp` can take care of this transfer.
    - From the directory `server.jar` is in, run `java -jar server.jar`
    - Note the server's IP address displayed in the console (`192.168.42.5` below)
```
java -jar server.jar 
Compiled: Thu Apr 15 08:45:54 PDT 2021
----- N E T W O R K -----
wlan0 fe80:0:0:0:b99a:63d5:e140:89cd%wlan0
wlan0 192.168.42.5
-------------------------
Use [Ctrl-C] to exit.
Chat server started on port 7001.
```

#### Client side
- Send the `client.jar` (newly generated in the `dist` folder) to all the machines you want to be able to take part of the chat network.
    - `scp` can take care of this transfer.
    - From the directory `client.jar` is in, run `java -jar client.jar --server-name:AAA.BBB.CC.DD`, where
      `AAA.BBB.CC.DD` is the server's IP address (`192.168.42.5` below).
    - _As an example_, there is an option `-s:true|false` or `--client-speech:true|false` (available on Linux or Mac for now) to make the client _speak_ the received messages.   
```
java -jar client.jar --server-name:192.168.42.5
Compiled: Thu Apr 15 08:45:55 PDT 2021
Done with client initialization.
>>> Telling server who I am: I_AM:raspi-4
Q or QUIT to quit
WHO_S_THERE to know who's there
Anything else will be broadcasted
> _
```      

### Next
Based on the same architecture, it would be easy to:
- Send a message to one client only (instead of broadcasting to everyone)
- Use non-text messages (binary, `json`, `yaml`, `xml`, etc) to communicate between applications on different machines
- etc
     
---
