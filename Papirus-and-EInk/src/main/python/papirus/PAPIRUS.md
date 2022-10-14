## Papirus, from Pi-Supply

Setup instructions:
 - Hardware [here](https://www.pi-supply.com/make/papirus-assembly-tips-and-gotchas/).
 - Software [here](https://github.com/repaper/gratis) and [here](https://github.com/PiSupply/PaPiRus).
 
---

Communicating with the screen can be done from the System command line, using the commands like `papirus-write`
(they are all described [here](https://github.com/PiSupply/PaPiRus#command-line)).

Listening to the buttons... could be a bit more tricky. Working on it.

### Get it up and running
On the pi, from <https://github.com/PiSupply/PaPiRus>:
```
$ curl -sSL https://pisupp.ly/papiruscode | sudo bash
```

There is *a lot* of examples in the repo at <https://github.com/PiSupply/PaPiRus>.

### WiP
As long as the Java drivers are not implemented yet, communication can be established through HTTP and REST.

Install and setup the Papirus as explained [above](https://github.com/PiSupply/PaPiRus).
 
Then do this:
- On the Raspberry Pi with the Papirus hooked-up on it:
```
$ python3 papirus_server.py --machine-name:$(hostname -I) [ --port:8888 --verbose:true ]
Starting server on port 8080
Try curl -X GET http://192.168.42.36:8080//papirus/oplist

```
- On any machine on the same network, including the Raspberry Pi hosting the Papirus:
```
$ curl --location --request POST 'http://192.168.42.36:8080/papirus/display?font_size=40' \
       --header 'Content-Type: text/plain' \
       --data-raw 'Display something!'
```
... where `192.168.42.36` is the IP of the Raspberry Pi.

Hit `Ctrl-C` in the Raspberry Pi's console to stop the server.

### TODO
- Button management
- A TCP client for the NMEA-multiplexer

---
