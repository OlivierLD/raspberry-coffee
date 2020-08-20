# PaPiRus

Instructions:
 - Hardware [here](https://www.pi-supply.com/make/papirus-assembly-tips-and-gotchas/).
 - Software [here](https://github.com/repaper/gratis) and [here](https://github.com/PiSupply/PaPiRus).
 
---

Communicating with the screen can be done from the System command line, using the commands like `papirus-write`
(they are all described [here](https://github.com/PiSupply/PaPiRus#command-line)).

Listening to the buttons... could be a bit more tricky. Working on it.

### TODO (soon)
Communication through HTTP and REST.


### Tentative
On the pi, from <https://github.com/PiSupply/PaPiRus>:
```
$ curl -sSL https://pisupp.ly/papiruscode | sudo bash
```

There is *a lot* of examples in the repo at <https://github.com/PiSupply/PaPiRus>.
