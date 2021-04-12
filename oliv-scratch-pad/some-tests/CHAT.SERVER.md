# TCP Based Java Chat Server
This is presented as a small example, it does not pretend to go to production.  
The goal here is to show the scaffolding for such a server, while keeping the code as small as possible.

It provides the following basic features (which you can extend):
- Messages are all text messages
- When a client is sending a message, it is re-broadcasted to all _other_ connected clients.
- If the server is taken down, all still-connected clients are notified.

Look into the package `oliv.events`.  
It can be compiled and distributed by using the script `package.chatserver.sh`.
Same script provides instructions on how to run the server and the client.

---
