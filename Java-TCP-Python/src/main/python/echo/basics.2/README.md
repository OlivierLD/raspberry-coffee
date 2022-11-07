# Python TCP basics
> Adapted from <https://realpython.com/python-sockets/>

From one terminal:
```
$ python echo-server.py
```

And from another terminal:
```
$ python echo-client.py
```

Server will manage one client at a time.  
Client is prompted to send a message to the server, the server echoes the message it received.  
If the client sends a `shutdown` message (not case-sensitive), the server terminates.
