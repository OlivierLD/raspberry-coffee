# Case Study (WiP)
> I want to run a simple HTTP server on my own box,
> possibly managing REST requests.  
> How can I do that?

## Here is an example use case

- You have run some `gradle test`, and you want to see their result on their web page.
- From the directory you did the `gradle test` from, you want to see the test results, from a browser
- You'd like to reach a url like <http://192.168.1.18:9876/build/reports/tests/test/index.html> ...

This requires some kind of small HTTP server, to server this HTTP request.

And several options are available.


## Python
Port default value is 8000, if the variable (`HTTP_PORT` below) is not set.
```text
$ HTTP_PORT=8000
$ python3 -m http.server ${HTTP_PORT}
```

## NodeJS
Requires a little bit of code, like this [`server.js`](../RESTNavServer/launchers/web/scratch/fax-processor.v2/server.js).  
Then run `node server.js`. The port number is set in the code.

## php
To know if a php server is installed, type
```
 $ which php
```
or
```
 $ php -v
   PHP 7.1.16 (cli) (built: Mar 31 2018 02:59:59) ( NTS )
   Copyright (c) 1997-2018 The PHP Group
   Zend Engine v3.1.0, Copyright (c) 1998-2018 Zend Technologies
```
If no php server is available, you can install one.
#### On Raspberry Pi
```
 $ sudo apt-get install php libapache2-mod-php
```

### Run a PHP server locally

```
$ HOSTNAME=$(hostname -I | awk '{ print $1 }' 2>/dev/null) || HOSTNAME=localhost
$ php -S ${HOSTNAME}:3000
```
Use the server's IP if you want to reach it from another box, not `localhost`.

## Java (_this_ module)
Aha! Now we're talking.  
Build the project (`../gradlew shadowJar`), and run the script `small.server.sh`.
Look into it for details.

---
