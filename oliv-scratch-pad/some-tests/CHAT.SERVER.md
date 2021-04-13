# TCP Based Java Chat Server
This is presented as a small example, it does not pretend to go to production.  
> The goal here is to show the scaffolding for such a server, while keeping the code as small as possible.

It provides the following basic features (which you can extend):
- Messages are all text messages
- When a client is sending a message, it is re-broadcasted to all _other_ connected clients.
- If the server is taken down, all still-connected clients are notified.

Look into the package `oliv.events`.  
It can be compiled and distributed by using the script `package.chatserver.sh`.
Same script provides instructions on how to run the server and the client.
The script produces 2 jars in the `dist` sub-folder, just above ~10Kb big.

### Get started, quick:  
- From the folder it's in, run the script `package.chatserver.sh`
- Send the `server.jar` (in the `dist` folder) to the machine you want to run the server on.
    - From the directory `server.jar` is in, run `java -jar server.jar`
    - Note the server's IP address displayed in the console
- Send the `client.jar` (in the `dist` folder) to all the machines you want to be able to take part of the chat network
    - From the directory `client.jar` is in, run `java -jar client.jar --server-name:aaa.bbb.cc.dd`, where
      `aaa.bbb.cc.dd` is the server's IP address.
      
---
