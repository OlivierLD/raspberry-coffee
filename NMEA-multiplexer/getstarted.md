# Get started, from scratch, fast.

The first thing to do is to get the code and compile it, so it can be executed.  
Then we will get some NMEA Data - from a USB GPS for example. If we can read that stream, then we're good to move on.

## Get the code, compile it
#### Pre-requisites
- A Raspberry Pi, with a recent `RasPi OS` installed on it
    - The Raspberry Pi Zero and A might be a bit weak to compile all the code (possible though, but you'd need to go under the hood). Models B would be preferred.
    - A network access is required to download and compile the code. Not necessary after that step. 
- `git`, already installed on your Raspberry Pi
- `java`, already installed on your Raspberry Pi
    - You will need at least `Java 8`, verify your java version with typing `java -version` in a terminal.
- `libxrtx`, to be installed (just once)
    - from a terminal, just do a `sudo apt-get install librxtx-java`
#### Get the code
We will clone the git repository. From a place of your choice (like your home directory), 
in a terminal, do a 
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
Let us do a test with a Serial (USB) GPS, like [this one](https://www.amazon.com/Onyehn-Navigation-External-Receiver-Raspberry/dp/B07GJGSZB9/ref=sr_1_5?crid=AXIK022XF9XZ&dchild=1&keywords=usb+gps+dongle&qid=1614448258&sprefix=USB+GPS%2Caps%2C217&sr=8-5).  
The first thing to do is to know what serial port to read.  
To do so, unplug the GPS from its USB port, and in a terminal, type
```
$ ls -l /dev/tty* > before.txt
```
Now plug your GPS in its USB port on the Raspberry Pi, give it a couple of seconds to get recognized, and type
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

> Note: you will have to do this link (`ln` command) every time the system re-starts.  
> It might be a good idea to put it in your `/etc/rc.local` file...

## First tests
So, we know that the port we want to read is `/dev/ttyS80`. We now need to know the **baud rate** (data speed)
we need to use. You will find this info in the documentation of your GPS. It is likely to 
be something like `4800`.

#### Good to start
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
It cannot be any simpler, it tells the Multiplexer (aka `mux`) to:
- Read the serial port `/dev/ttyS80` at `4800` bits per second
- Spit out whatever is read on the terminal (the console)

> Note: Change the port name to fit your settings, if needed.

From the `NMEA-multiplexer` directory, let's do it:
```
$ ./mux.sh nmea.mux.1st.test.yaml 
```
You should see an output like that:
```
Using properties file nmea.mux.1st.test.yaml
Running sudo java  -Djava.library.path=/usr/lib/jni -Dscreen.verbose=true -Drest.feeder.verbose=true -Dparse.ais=true -Dmux.props.verbose=true -Dprocess.on.start=true -Dmux.properties=nmea.mux.1st.test.yaml -Dno.ais=false -Dcalculate.solar.with.eot=true -Ddefault.mux.latitude=37.8218 -Ddefault.mux.longitude=-122.3112 -Dtry.to.speak=true -Djava.util.logging.config.file=./logging.properties   -cp ./build/libs/NMEA-multiplexer-1.0-all.jar:/usr/share/java/RXTXcomm.jar nmea.mux.GenericNMEAMultiplexer
Definition Name: First test
{with.http.server=false, init.cache=false}
Stable Library
=========================================
Native lib Version = RXTX-2.2pre2
Java lib Version   = RXTX-2.1-7
WARNING:  RXTX Version mismatch
	Jar version = RXTX-2.1-7
	native lib Version = RXTX-2.2pre2
Port Ownership of /dev/ttyS80 changed: type=1, Owned (Locked)
This is a serial port
Reading serial port...
/dev/ttyS80:4800  > Port is open...
$GNRMC,180325.00,A,3744.92708,N,12230.43228,W,0.224,,270221,,,D*79
$GNVTG,,T,,M,0.224,N,0.415,K,D*3C
$GNGGA,180325.00,3744.92708,N,12230.43228,W,2,10,1.05,-13.2,M,-29.9,M,,0000*64
$GNGSA,A,3,04,09,27,07,30,08,,,,,,,1.99,1.05,1.69*13
$GNGSA,A,3,71,72,73,65,,,,,,,,,1.99,1.05,1.69*13
$GPGSV,4,1,13,03,02,171,,04,37,121,22,05,07,324,,07,65,317,10*7D
$GPGSV,4,2,13,08,27,117,08,09,77,109,35,14,15,212,,16,17,042,*7F
$GPGSV,4,3,13,27,24,075,23,28,10,211,,30,35,276,16,46,46,191,*7D
$GPGSV,4,4,13,51,43,156,*4A
$GLGSV,2,1,08,65,43,323,25,71,34,159,17,72,82,229,20,73,20,041,25*65
$GLGSV,2,2,08,74,64,001,,75,43,254,31,82,01,011,,83,05,053,*6A
$GNGLL,3744.92708,N,12230.43228,W,180325.00,A,D*60
$GNTXT,01,01,02,u-blox AG - www.u-blox.com*4E
$GNTXT,01,01,02,HW UBX-M8030 00080000*60
$GNTXT,01,01,02,ROM CORE 3.01 (107888)*2B
$GNTXT,01,01,02,FWVER=SPG 3.01*46
$GNTXT,01,01,02,PROTVER=18.00*11
$GNTXT,01,01,02,GPS;GLO;GAL;BDS*77
$GNTXT,01,01,02,SBAS;IMES;QZSS*49
$GNTXT,01,01,02,GNSS OTP=GPS;GLO*37
$GNTXT,01,01,02,LLC=FFFFFFFF-FFFFFFFF-FFFFFFFF-FFFFFFFF-FFFFFFFD*2F
$GNTXT,01,01,02,ANTSUPERV=AC SD PDoS SR*3E
$GNTXT,01,01,02,ANTSTATUS=OK*25
$GNTXT,01,01,02,PF=3FF*4B
$GNRMC,180326.00,A,3744.92703,N,12230.43207,W,0.303,,270221,,,D*78
$GNVTG,,T,,M,0.303,N,0.561,K,D*3A
$GNGGA,180326.00,3744.92703,N,12230.43207,W,2,10,1.08,-13.2,M,-29.9,M,,0000*6C
$GNGSA,A,3,04,09,27,07,30,08,,,,,,,2.04,1.08,1.73*12
$GNGSA,A,3,71,72,73,65,,,,,,,,,2.04,1.08,1.73*12
$GPGSV,4,1,13,03,02,171,,04,37,121,23,05,07,324,,07,65,317,10*7C
$GPGSV,4,2,13,08,27,117,08,09,77,109,35,14,15,212,,16,17,042,*7F
$GPGSV,4,3,13,27,24,075,22,28,10,211,,30,35,276,15,46,46,191,*7F
$GPGSV,4,4,13,51,43,156,*4A
$GLGSV,2,1,08,65,43,323,25,71,34,159,17,72,82,229,20,73,20,041,26*66
$GLGSV,2,2,08,74,64,001,,75,43,254,31,82,01,011,,83,05,053,*6A
$GNGLL,3744.92703,N,12230.43207,W,180326.00,A,D*65
$GNRMC,180327.00,A,3744.92748,N,12230.43212,W,1.578,,270221,,,D*79
$GNVTG,,T,,M,1.578,N,2.923,K,D*39
$GNGGA,180327.00,3744.92748,N,12230.43212,W,2,10,1.05,-12.5,M,-29.9,M,,0000*6D
$GNGSA,A,3,04,09,27,07,30,08,,,,,,,1.99,1.05,1.69*13
$GNGSA,A,3,71,72,73,65,,,,,,,,,1.99,1.05,1.69*13
$GPGSV,4,1,13,03,02,171,,04,37,121,22,05,07,324,,07,65,317,14*79
$GPGSV,4,2,13,08,27,117,09,09,77,109,33,14,15,212,,16,17,042,*78
$GPGSV,4,3,13,27,24,075,21,28,10,211,,30,35,276,13,46,46,191,*7A
$GPGSV,4,4,13,51,43,156,*4A
$GLGSV,2,1,08,65,43,323,24,71,34,159,17,72,82,229,20,73,20,041,26*67
$GLGSV,2,2,08,74,64,001,,75,43,254,30,82,01,011,,83,05,053,*6B
$GNGLL,3744.92748,N,12230.43212,W,180327.00,A,D*6F
. . .
```
To stop the program, just type [Ctrl-C] in the console.
```
^CShutting down multiplexer nicely.
- Stop reading nmea.consumers.reader.SerialReader
Port Ownership /dev/ttyS80 changed: type=2, UnOwned (Released)
- Stop writing to the console. (nmea.forwarders.ConsoleWriter)
$ 
```

You're good!

## Next steps
You can now dive into the [manual](./manual.md).

---
