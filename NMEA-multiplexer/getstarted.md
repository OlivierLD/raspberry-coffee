# Get Started, from scratch, fast.

The first thing to do is to get the code and compile it, so it can be executed.  
Then we will get some NMEA Data - from a USB GPS for example. If we can read that stream, this is good.

## Get the code, compile it
#### Pre-requisites
- `git`, already installed on your Raspberry Pi
- `java`, already installed on your Raspberry Pi
    - You will need at least `Java 8`, verify your java version with typing `java -version` in a terminal.
- `libxrtx`, to be installed
    - from a terminal, just do a `sudo apt-get install librxtx-java`
#### Get the code
We will clone the git repository, from a place of you choice (like your home directory), 
from a terminal, do a 
```
$ git clone https://github.com/OlivierLD/raspberry-coffee.git
```
This can take several minutes...  
Now, we compile the NMEA Multiplexer. In a terminal, do this:
```
$ cd raspberry-coffee/NMEA-multiplexer
$ ../gradlew clean shadowJar --parallel
```
If `gradle` has not yet been used on your system, this last command will download it first.  
If the previous command goes well, we are ready for the first tests. 

## Serial ?
Let us do  test with a Serial (USB) GPS, like [this one](https://www.amazon.com/Onyehn-Navigation-External-Receiver-Raspberry/dp/B07GJGSZB9/ref=sr_1_5?crid=AXIK022XF9XZ&dchild=1&keywords=usb+gps+dongle&qid=1614448258&sprefix=USB+GPS%2Caps%2C217&sr=8-5).  
The first thing to do is to know what serial port to read.  
To do so, unplug the GPS from its USB port, and in a terminal, type
```
$ ls -l /dev/tty* > before.txt
```
Then plug your GPS in its USB port on the Raspberry Pi, give it a couple of seconds to get recognized, and type
```
$ ls -l /dev/tty* > after.txt
```
Then:
```
$ diff before.txt after.txt
65a66
> crw-rw---- 1 root dialout 166, 0 Feb 27 09:11 /dev/ttyACM0
```
`/dev/ttyACM0` is your serial port.

### Another option
In a terminal, type
```
$ ./serial.util.sh
$ ./serial.util.sh 
Stable Library
=========================================
Native lib Version = RXTX-2.2pre2
Java lib Version   = RXTX-2.1-7
WARNING:  RXTX Version mismatch
	Jar version = RXTX-2.1-7
	native lib Version = RXTX-2.2pre2

----- Serial Port List -----
Found 0 port(s)
----------------------------
```
##### Warning!
If - as above - you do not see your `/dev/ttyACM0` port, it is a known problem, and we are going to fix it. 
Let us create a symbolic link. In a terminal, type
```
$ sudo ln -s /dev/ttyACM0 /dev/ttyS80
```
Now, re-launch `serial.util.sh`:
```
$ ./serial.util.sh 
Stable Library
=========================================
Native lib Version = RXTX-2.2pre2
Java lib Version   = RXTX-2.1-7
WARNING:  RXTX Version mismatch
	Jar version = RXTX-2.1-7
	native lib Version = RXTX-2.2pre2

----- Serial Port List -----
Port: /dev/ttyS80, SERIAL, free.
Found 1 port(s)
----------------------------
```
The port to read will now be `/dev/ttyS80`

> Note: you will have to do this link (`ln` command) everytime the system re-starts.  
> It might be a good idea to put it in your `/etc/rc.local` file...

## First tests
So, we know that the port we want to read is `/dev/ttyS80`. We now need to know the **baud rate** (data speed)
we need to use. You will find this info in the documentation of your GPS. It is likely to 
be something like `4800`.

## Good to go
We will start the `NMEA-multiplexer`, driven by the file `nmea.mux.1st.test.yaml`:
```yaml
#
# MUX definition.
#
name: "First test"
context:
  with.http.server: false
  init.cache: false
channels:
  - type: serial
    port: /dev/ttyS80
    baudrate: 4800
    verbose: false
forwarders:
  - type: console
```
It cannot be any simpler, it tells the Multiplexer (aka mux) to
- Read the serial port `/dev/ttyS80`
- Spit out whatever is read in a console

From the `NMEA-multiplexer` directory, let's do it:
```
$ ./mux.sh nmea.mux.1st.test.yaml 
```