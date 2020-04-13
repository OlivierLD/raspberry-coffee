# Various REST clients for various REST services
This is not about Web clients here, we are talking about Java (and other JVM compatible) clients.

### TCP Watch
So you do not need to connect to a smart-phone via BlueTooth to get to the network.

### Exposing Sensors through REST, assembling with Node-RED
##### Micro-services, IoT, etc
Very trending.
 
- Sensor Code (BMP280, LSM303, Servos, etc)
- Expose to HTTP
    - With the HTTPServer in the `http-tiny-server` project (in this repo)
    - As a Micro Service, with [Helidon](https://helidon.io), [Micronaut](https://micronaut.io/), or similar <!--[fnProject](http://fnproject.io)-->
        - Swagger (aka OpenAPI) ? Definition and generation
- Assemble a flow with [Node-RED](https://nodered.org/) and run it

