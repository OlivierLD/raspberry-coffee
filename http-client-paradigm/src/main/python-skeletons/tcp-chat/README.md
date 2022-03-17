# TCP Chat Server, and Client
## A working simple example.

This presents a multi-threaded server, and the corresponding client (several client instances can be spawned, lucky us!).

It is implementing only the basic features of a chat server. 
One of the requirements was to keep the code as small as possible.  
You need the clients and the server to be on a network where they can see each other, obviously.

### Requirements
Written for python3. This should work everywhere Python3 runs.  
Required modules
- `socket`
- `sys`
- `traceback`
- `threading`
- `json`

Those modules should be available in python3, without having to install them.

### Get Started
> _**Note**_: If server and clients are not running on the same machine (like in the real world),
> then you need to know the name - or IP address of the server, as explained below.  
> If - like for tests - server and client(s) run on the box, then
> you can drop the `---machine-name:` parameter in the commands below.

> On a Raspberry Pi, you can get the IP address of a machine, by typing `hostname -I`.  
> This might not be the case on other systems...

From one terminal, from one machine, start the chat server:
```text
$ python3 tcp_chat_server.py --machine-name:$(hostname -I)
```
and that guy will wait for client connections. Notice the `$(hostname -I)`, you will need it for the subsequent clients.
You may store it in a system variable called `SERVER_IP`:
```text
$ export SERVER_IP=$(hostname -I)
```

From another terminal, on the same machine, or on another one on the same network 
(make sure `SERVER_IP` is positioned as expected, with the IP address **of the server**):
```text
$ python3 tcp_chat_client.py --machine-name:${SERVER_IP}
```
Let's say - for the example - that the **server**'s IP is `192.168.1.18`.  
_After starting the server_, you can start the client(s), you would do it like:
```text
$ python3 tcp_chat_client.py --machine-name:192.168.1.18
```

Then one time only, you will be prompted for a user name:
```text
Usage is:
python3 tcp_chat_client.py [--machine-name:127.0.0.1] [--port:7002] [--verbose:true|false]
	where --machine-name: and --port: must match the server's settings.

connecting to 192.168.1.18 port 7002
...Connected
To exit, type Q, QUIT, or EXIT (lower or upper case)
To get the list of connected clients, type L (lower or upper case)
Your name> tintin
You say  > 
```
From now on, you can:
- Request the list of the connected clients (type `L` at the prompt)
  ```text
  Your name> tintin
  You say  > L
  You say  > 2 Client(s):
  - Client [haddock]
  - Client [tintin]
    ```
- Send a message to other user(s)
  - Enter the message at the `You say  >` prompt
  - Then it will ask you who to send it to
    - You can give the name of a user, or say `ALL` to send it to everyone (including yourself)
  ```text
  You say  > Bachi-bouzouk!
  Dest name> haddock
  ```
  Message will be received by the receiver (`haddock` here) as
  ```text
  Message from tintin: Bachi-bouzouk!
  ```
To exit the client, type `Q`, `QUIT`, or `EXIT` at the prompt.  
To quit the server, `Ctrl-C` in the terminal.

### Some things to note
The client is multi-threaded too. The main thread sends the requests to the server,
another thread is receiving the messages from the server.

--- 
