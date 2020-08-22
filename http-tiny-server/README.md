## HTTP Server (WiP, never finished...)
Can be used
- to serve
    - static HTML (and related) documents (from the file system, or archived in a zip)
    - REST requests
- as an HTTP Proxy

**Designed to be as tiny and small as possible**, to run on small boards (like the Raspberry Pi Zero).

- No security (SSL) available. 
- ~~_Not even multi-threaded_~~. Nov 2019: Now multi-threaded
- Some restrictions exist (`100-continue`, Multipart, binary data, CORS, ...)
- Not compliant with _any_ coding standard, like JAX-RS, MicroProfile, JEE, etc

> May 2019: The archive containing the core classes is just above _**2M**_ big. Compare it to micro servers like Helidon or SpringBoot...
> Helidon or SpringBoot would definitely run on a Raspberry Pi, I've tested them. But again,
> the goal here is to be HTTP and REST compliant (any REST or HTTP client would work), and remain as small as possible. 

Logging available.

Some special resources are reserved, like `/exit`, `/test`, feel free to comment them if needed.

The constructor of the `HTTPServer` class can take a `Properties` object as parameter.
Some of its members will be detailed below.

They are:
- `static.docs`
- `static.zip.docs`
- `autobind`
- `web.archive`

If `autobind` exists and is set to `true`, no `BindException` will be raised even if the specified port is busy. The default port (or the one given in `-Dhttp.port`) will be incremented until a free one is found. 

It also comes with a Java client so you can make HTTP requests from your Java code, see [`http.client.HTTPClient.java`](src/main/java/http/client/HTTPClient.java).

##### Static pages
Driven by the `static.docs` property, of the `Properties` object mentioned above. Defaulted to `/web/`.
Whatever request points to this resource(s) will be treated as a static request.

> Example: you started the `HTTPServer` with the default properties.
> A request like `http://localhost:9999/web/index.html` will look for an `index.html` in a `web` directory
> under the directory the `HTTPServer` has been started from.

> Example: from a browser, reach `http://localhost:9999/test`, you should see a page saying "Test is OK".

See the code (comments and javadoc) for more details.

##### REST server
To serve REST requests, you need to implement a `RESTRequestManager` interface.
Several examples are available in this project.
The class implementing the `RESTRequestManager` usually delegates the job to a `RESTImplementation` (examples available as well).

The `RESTRequestManager` (the class implementing it) is a possible parameter of the `HTTPServer`'s constructor.

A given `HTTPServer` can register several `RESTRequestManager`s, like in `navrest.NavServer`, in the `RESTNavServer` module:

```java
 this.httpServer = startHttpServer(httpPort, new NavRequestManager(this));
 // Add astronomical features...
 this.httpServer.addRequestManager(new AstroRequestManager());
 // Add tide features...
 this.httpServer.addRequestManager(new TideRequestManager());
 // Add Nav features: Dead Reckoning, logging, re-broadcasting, from the NMEA Multiplexer
 Properties definitions = GenericNMEAMultiplexer.getDefinitions();
 this.httpServer.addRequestManager(new GenericNMEAMultiplexer(definitions)); // refers to nmea.mux.properties, unless -Dmux.properties is set
 // Add image processing service...
 this.httpServer.addRequestManager(new ImgRequestManager());
 // Add GRIB features
 this.httpServer.addRequestManager(new GRIBRequestManager());
 // Add SunFlower, for sun data
 this.httpServer.addRequestManager(new SunFlowerRequestManager());

```

##### Proxy
_If_:
- a request is not a static request
- there is no `RESTRequestManager` registered

then the server might be acting as a proxy.

For the server to act as a proxy, you need to register a `proxyFunction`.

If a `proxyFunction` has been registered, then you have a proxy ;)

See in `HTTPServer` the `defaultProxy`, and the way it is registered:
```java
  HTTPServer httpServer = new HTTPServer(9999);
  httpServer.setProxyFunction(HTTPServer::defaultProxy);
```

The `defaultProxy` only does some logging. It is a `Function<HTTPServer.Request, HTTPServer.Response>`.
It's very easy to implement your own.

##### Example
After building the project:
```
 $> ../gradlew shadowJar
````
Run the `HTTPServer` as it is (it comes with a `main`, for illustration):
```
CP=build/libs/http-tiny-server-1.0.jar
JAVA_OPTIONS=
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.verbose.dump=true"
JAVA_OPTIONS="$JAVA_OPTIONS -Dhttp.client.verbose=true"
#
JAVA_OPTIONS="$JAVA_OPTIONS -Djava.util.logging.config.file=logging.properties"
#
java -cp $CP $JAVA_OPTIONS http.HTTPServer
```
Then, from an application like PostMan (for example), set the proxy to `localhost:9999`, and
make a request from PostMan. You should see an output like this in the console you started the proxy from:
```
Started
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 47 45 54 20 68 74 74 70 3A 2F 2F 31 39 32 2E 31  |  GET http://192.1
	01 | 36 38 2E 34 32 2E 31 37 3A 38 30 38 38 2F 70 77  |  68.42.17:8088/pw
	02 | 73 2F 6F 70 6C 69 73 74 20 48 54 54 50 2F 31 2E  |  s/oplist HTTP/1.
	03 | 31                                               |  1
	---+--------------------------------------------------+------------------

GET http://192.168.42.17:8088/pws/oplist HTTP/1.1
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 63 61 63 68 65 2D 63 6F 6E 74 72 6F 6C 3A 20 6E  |  cache-control: n
	01 | 6F 2D 63 61 63 68 65                             |  o-cache
	---+--------------------------------------------------+------------------

cache-control: no-cache
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 50 6F 73 74 6D 61 6E 2D 54 6F 6B 65 6E 3A 20 35  |  Postman-Token: 5
	01 | 65 63 33 65 30 34 31 2D 32 34 35 63 2D 34 31 65  |  ec3e041-245c-41e
	02 | 39 2D 61 62 30 32 2D 63 35 36 65 35 64 66 34 38  |  9-ab02-c56e5df48
	03 | 38 64 38                                         |  8d8
	---+--------------------------------------------------+------------------

Postman-Token: 5ec3e041-245c-41e9-ab02-c56e5df488d8
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 55 73 65 72 2D 41 67 65 6E 74 3A 20 50 6F 73 74  |  User-Agent: Post
	01 | 6D 61 6E 52 75 6E 74 69 6D 65 2F 37 2E 31 2E 35  |  manRuntime/7.1.5
	02 |                                                  |
	---+--------------------------------------------------+------------------

User-Agent: PostmanRuntime/7.1.5
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 41 63 63 65 70 74 3A 20 2A 2F 2A                 |  Accept: */*
	---+--------------------------------------------------+------------------

Accept: */*
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 48 6F 73 74 3A 20 31 39 32 2E 31 36 38 2E 34 32  |  Host: 192.168.42
	01 | 2E 31 37 3A 38 30 38 38                          |  .17:8088
	---+--------------------------------------------------+------------------

Host: 192.168.42.17:8088
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 61 63 63 65 70 74 2D 65 6E 63 6F 64 69 6E 67 3A  |  accept-encoding:
	01 | 20 67 7A 69 70 2C 20 64 65 66 6C 61 74 65        |   gzip, deflate
	---+--------------------------------------------------+------------------

accept-encoding: gzip, deflate
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 43 6F 6E 6E 65 63 74 69 6F 6E 3A 20 6B 65 65 70  |  Connection: keep
	01 | 2D 61 6C 69 76 65                                |  -alive
	---+--------------------------------------------------+------------------

Connection: keep-alive



                                                                        Response code: 200
                                                                        ---+--------------------------------------------------+------------------
                                                                           |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
                                                                        ---+--------------------------------------------------+------------------
                                                                        00 | 6E 75 6C 6C 3A 20 48 54 54 50 2F 31 2E 31 20 32  |  null: HTTP/1.1 2
                                                                        01 | 30 30                                            |  00
                                                                        ---+--------------------------------------------------+------------------
                                                                        ---+--------------------------------------------------+------------------
                                                                           |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
                                                                        ---+--------------------------------------------------+------------------
                                                                        00 | 41 63 63 65 73 73 2D 43 6F 6E 74 72 6F 6C 2D 41  |  Access-Control-A
                                                                        01 | 6C 6C 6F 77 2D 4F 72 69 67 69 6E 3A 20 2A        |  llow-Origin: *
                                                                        ---+--------------------------------------------------+------------------
                                                                        ---+--------------------------------------------------+------------------
                                                                           |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
                                                                        ---+--------------------------------------------------+------------------
                                                                        00 | 43 6F 6E 74 65 6E 74 2D 4C 65 6E 67 74 68 3A 20  |  Content-Length:
                                                                        01 | 39 30 32                                         |  902
                                                                        ---+--------------------------------------------------+------------------
                                                                        ---+--------------------------------------------------+------------------
                                                                           |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
                                                                        ---+--------------------------------------------------+------------------
                                                                        00 | 43 6F 6E 74 65 6E 74 2D 54 79 70 65 3A 20 61 70  |  Content-Type: ap
                                                                        01 | 70 6C 69 63 61 74 69 6F 6E 2F 6A 73 6F 6E        |  plication/json
                                                                        ---+--------------------------------------------------+------------------

                                                                        null: HTTP/1.1 200
                                                                        Access-Control-Allow-Origin: *
                                                                        Content-Length: 902
                                                                        Content-Type: application/json
                                                                        ---+--------------------------------------------------+------------------
                                                                           |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
                                                                        ---+--------------------------------------------------+------------------
                                                                        00 | 5B 7B 22 76 65 72 62 22 3A 22 47 45 54 22 2C 22  |  [{"verb":"GET","
                                                                        01 | 70 61 74 68 22 3A 22 2F 70 77 73 2F 6F 70 6C 69  |  path":"/pws/opli
                                                                        02 | 73 74 22 2C 22 64 65 73 63 72 69 70 74 69 6F 6E  |  st","description
                                                                        03 | 22 3A 22 4C 69 73 74 20 6F 66 20 61 6C 6C 20 61  |  ":"List of all a
                                                                        04 | 76 61 69 6C 61 62 6C 65 20 6F 70 65 72 61 74 69  |  vailable operati
                                                                        05 | 6F 6E 73 2E 22 2C 22 66 6E 22 3A 7B 7D 7D 2C 7B  |  ons.","fn":{}},{
                                                                        06 | 22 76 65 72 62 22 3A 22 47 45 54 22 2C 22 70 61  |  "verb":"GET","pa
                                                                        07 | 74 68 22 3A 22 2F 70 77 73 2F 73 74 68 31 30 2D  |  th":"/pws/sth10-
                                                                        08 | 64 61 74 61 22 2C 22 64 65 73 63 72 69 70 74 69  |  data","descripti
                                                                        09 | 6F 6E 22 3A 22 47 65 74 20 64 65 76 69 63 65 20  |  on":"Get device
                                                                        0A | 44 61 74 61 2E 20 54 65 6D 70 65 72 61 74 75 72  |  Data. Temperatur
                                                                        0B | 65 2C 20 68 75 6D 69 64 69 74 79 22 2C 22 66 6E  |  e, humidity","fn
                                                                        0C | 22 3A 7B 7D 7D 2C 7B 22 76 65 72 62 22 3A 22 47  |  ":{}},{"verb":"G
                                                                        0D | 45 54 22 2C 22 70 61 74 68 22 3A 22 2F 70 77 73  |  ET","path":"/pws
                                                                        0E | 2F 72 65 6C 61 79 2D 73 74 61 74 65 22 2C 22 64  |  /relay-state","d
                                                                        0F | 65 73 63 72 69 70 74 69 6F 6E 22 3A 22 47 65 74  |  escription":"Get
                                                                        10 | 20 72 65 6C 61 79 20 73 74 61 74 65 20 2D 20 4F  |   relay state - O
                                                                        11 | 4E 20 6F 66 20 4F 46 46 2E 22 2C 22 66 6E 22 3A  |  N of OFF.","fn":
                                                                        12 | 7B 7D 7D 2C 7B 22 76 65 72 62 22 3A 22 47 45 54  |  {}},{"verb":"GET
                                                                        13 | 22 2C 22 70 61 74 68 22 3A 22 2F 70 77 73 2F 6C  |  ","path":"/pws/l
                                                                        14 | 61 73 74 2D 77 61 74 65 72 69 6E 67 2D 74 69 6D  |  ast-watering-tim
                                                                        15 | 65 22 2C 22 64 65 73 63 72 69 70 74 69 6F 6E 22  |  e","description"
                                                                        16 | 3A 22 47 65 74 20 6C 61 73 74 20 77 61 74 65 72  |  :"Get last water
                                                                        17 | 69 6E 67 20 74 69 6D 65 20 61 73 20 61 20 6C 6F  |  ing time as a lo
                                                                        18 | 6E 67 2E 22 2C 22 66 6E 22 3A 7B 7D 7D 2C 7B 22  |  ng.","fn":{}},{"
                                                                        19 | 76 65 72 62 22 3A 22 47 45 54 22 2C 22 70 61 74  |  verb":"GET","pat
                                                                        1A | 68 22 3A 22 2F 70 77 73 2F 70 77 73 2D 73 74 61  |  h":"/pws/pws-sta
                                                                        1B | 74 75 73 22 2C 22 64 65 73 63 72 69 70 74 69 6F  |  tus","descriptio
                                                                        1C | 6E 22 3A 22 47 65 74 20 64 65 76 69 63 65 5C 75  |  n":"Get device\u
                                                                        1D | 30 30 32 37 73 20 73 74 61 74 75 73 2E 22 2C 22  |  0027s status.","
                                                                        1E | 66 6E 22 3A 7B 7D 7D 2C 7B 22 76 65 72 62 22 3A  |  fn":{}},{"verb":
                                                                        1F | 22 47 45 54 22 2C 22 70 61 74 68 22 3A 22 2F 70  |  "GET","path":"/p
                                                                        20 | 77 73 2F 70 77 73 2D 70 61 72 61 6D 65 74 65 72  |  ws/pws-parameter
                                                                        21 | 73 22 2C 22 64 65 73 63 72 69 70 74 69 6F 6E 22  |  s","description"
                                                                        22 | 3A 22 47 65 74 20 70 72 6F 67 72 61 6D 5C 75 30  |  :"Get program\u0
                                                                        23 | 30 32 37 73 20 70 61 72 61 6D 65 74 65 72 73 2E  |  027s parameters.
                                                                        24 | 22 2C 22 66 6E 22 3A 7B 7D 7D 2C 7B 22 76 65 72  |  ","fn":{}},{"ver
                                                                        25 | 62 22 3A 22 50 4F 53 54 22 2C 22 70 61 74 68 22  |  b":"POST","path"
                                                                        26 | 3A 22 2F 70 77 73 2F 73 74 68 31 30 2D 64 61 74  |  :"/pws/sth10-dat
                                                                        27 | 61 22 2C 22 64 65 73 63 72 69 70 74 69 6F 6E 22  |  a","description"
                                                                        28 | 3A 22 53 65 74 20 64 65 76 69 63 65 20 44 61 74  |  :"Set device Dat
                                                                        29 | 61 2E 20 54 65 6D 70 65 72 61 74 75 72 65 2C 20  |  a. Temperature,
                                                                        2A | 68 75 6D 69 64 69 74 79 2C 20 66 6F 72 20 73 69  |  humidity, for si
                                                                        2B | 6D 75 6C 61 74 69 6F 6E 22 2C 22 66 6E 22 3A 7B  |  mulation","fn":{
                                                                        2C | 7D 7D 2C 7B 22 76 65 72 62 22 3A 22 50 55 54 22  |  }},{"verb":"PUT"
                                                                        2D | 2C 22 70 61 74 68 22 3A 22 2F 70 77 73 2F 72 65  |  ,"path":"/pws/re
                                                                        2E | 6C 61 79 2D 73 74 61 74 65 22 2C 22 64 65 73 63  |  lay-state","desc
                                                                        2F | 72 69 70 74 69 6F 6E 22 3A 22 46 6C 69 70 20 74  |  ription":"Flip t
                                                                        30 | 68 65 20 72 65 6C 61 79 20 2D 20 4F 4E 20 6F 66  |  he relay - ON of
                                                                        31 | 20 4F 46 46 2E 22 2C 22 66 6E 22 3A 7B 7D 7D 2C  |   OFF.","fn":{}},
                                                                        32 | 7B 22 76 65 72 62 22 3A 22 50 55 54 22 2C 22 70  |  {"verb":"PUT","p
                                                                        33 | 61 74 68 22 3A 22 2F 70 77 73 2F 70 77 73 2D 70  |  ath":"/pws/pws-p
                                                                        34 | 61 72 61 6D 65 74 65 72 73 22 2C 22 64 65 73 63  |  arameters","desc
                                                                        35 | 72 69 70 74 69 6F 6E 22 3A 22 53 65 74 20 74 68  |  ription":"Set th
                                                                        36 | 65 20 50 72 6F 67 72 61 6D 5C 75 30 30 32 37 73  |  e Program\u0027s
                                                                        37 | 20 70 61 72 61 6D 65 74 65 72 73 22 2C 22 66 6E  |   parameters","fn
                                                                        38 | 22 3A 7B 7D 7D 5D                                |  ":{}}]
                                                                        ---+--------------------------------------------------+------------------

                                                                        [{"verb":"GET","path":"/pws/oplist","description":"List of all available operations.","fn":{}},{"verb":"GET","path":"/pws/sth10-data","description":"Get device Data. Temperature, humidity","fn":{}},{"verb":"GET","path":"/pws/relay-state","description":"Get relay state - ON of OFF.","fn":{}},{"verb":"GET","path":"/pws/last-watering-time","description":"Get last watering time as a long.","fn":{}},{"verb":"GET","path":"/pws/pws-status","description":"Get device\u0027s status.","fn":{}},{"verb":"GET","path":"/pws/pws-parameters","description":"Get program\u0027s parameters.","fn":{}},{"verb":"POST","path":"/pws/sth10-data","description":"Set device Data. Temperature, humidity, for simulation","fn":{}},{"verb":"PUT","path":"/pws/relay-state","description":"Flip the relay - ON of OFF.","fn":{}},{"verb":"PUT","path":"/pws/pws-parameters","description":"Set the Program\u0027s parameters","fn":{}}]

```

Even simpler, you can use `curl`:
```
$> curl -x http://localhost:9999/ http://192.168.42.17:8088/pws/oplist
 [{"verb":"GET","path":"/pws/oplist","description":"List of all available operations.","fn":{}},{"verb":"GET","path":"/pws/sth10-data","description":"Get device Data. Temperature, humidity","fn":{}},{"verb":"GET","path":"/pws/relay-state","description":"Get relay state - ON of OFF.","fn":{}},{"verb":"GET","path":"/pws/last-watering-time","description":"Get last watering time as a long.","fn":{}},{"verb":"GET","path":"/pws/pws-status","description":"Get device\u0027s status.","fn":{}},{"verb":"GET","path":"/pws/pws-parameters","description":"Get program\u0027s parameters.","fn":{}},{"verb":"POST","path":"/pws/sth10-data","description":"Set device Data. Temperature, humidity, for simulation","fn":{}},{"verb":"PUT","path":"/pws/relay-state","description":"Flip the relay - ON of OFF.","fn":{}},{"verb":"PUT","path":"/pws/pws-parameters","description":"Set the Program\u0027s parameters","fn":{}}]
```
This produces the same output as above in the proxy's console.

With a Graphical UI, run the class `utils.proxyguisample.ProxyGUI`:

![Proxy GUI](./ProxyGUI.png)

## Bonus
Not 100% related, but could be useful, there is the skeleton of a 
Python HTTP/REST server, along with some use-cases and scenarios, [here](../http-clients/src/main/python-skeletons/README.md).

---

