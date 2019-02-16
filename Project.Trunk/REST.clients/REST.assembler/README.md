# From scratch to a sensor flow

Let's says you have:
- A Soil Humidity sensor, planted in a pot, at your home
- A peristaltic pump, that can be started and stopped at will

Those two devices are connected to a Raspberry Pi that can read data from the sensor, and drive the pump.

Now, we want to expose those features to a network and build some logic around them, in order to start the pump when the soil
humidity drops below a given threshold.

REST clients can be programs, or Web pages.

## Create the Java REST services
We start from the code that allows you to read data emitted by various sensors,
many such examples are available in this repository.

> For examples:
>
> Look in the `I2C.SPI` module.

### Using Swagger (aka Open API)
[`Swagger`](https://swagger.io/) has been designed to facilitate the development of REST Services.
You can start from the service definition (in `json` or `yaml` format, `yaml` being the easiest to deal with), and then
you can generate the skeleton of your implementation (in the language of your choice), for the client, for the server, _as well as the documentation_ of your services,
based on the `json` or `yaml` definition you started from.
This documentation part is a very cool feature.

Interestingly, even if you do not intend to implement your application in NodeJS, you may very well
run the NodeJS generator, just to have the documentation web pages up and running.

For example:
> Note: We provide here a simple `sensors.yaml`, as an example. This is the file the `gradle` task below will start from.

- From the directory this page you're reading lives in, run
```
 $ ../../../gradlew swaggerNode
```
Among others, this will generate in its `node` directory a `package.json`.
Assuming you've installed NodeJS in your environment, do a 
```
 $ cd generated/node
 $ npm install
```
followed by a 
```
 $ node index.js
```
Then from a browser on the same machine, just reach http://localhost:8765/docs. 

![Swagger Doc](./img/swagger.01.png)

Without doing more, you can even try out the services you've defined.

> Note: If you want to implement the rest of the project in `NodeJS`, this is certainly possible.
> For more details, see the [`Node Pi`](https://github.com/OlivierLD/node.pi) project.

The services can be invoke from any REST client. `curl`, `PostMan`, a browser (for the `GET` requests`), your own code...

> Note: The UI above gives you the syntax of the `curl` requests, for example:
```
 $ curl -X GET --header 'Accept: application/json' 'http://localhost:8765/v1/sensors/pump'
```

#### Micro Service?
Serverless... Actually means that the server can be anywhere, everywhere, etc.

##### Helidon
Quite mature. Its `micro-profile` feature is appealing.

You need `Maven` to be available on your system (it's there by default on the Raspberry Pi).

Using `Maven`, let's create the require infrastructure.

You need network access to complete this step. If you are behind a firewall, you need to provide the 
proxy location by adding `-Dhttp.proxyHost=www-proxy.me.home -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.me.home -Dhttps.proxyPort=80`
to the command below (replace `www-proxy.me.home` with the appropriate value).
```
$ mvn archetype:generate -DinteractiveMode=false \
      -DarchetypeGroupId=io.helidon.archetypes \
      -DarchetypeArtifactId=helidon-quickstart-mp \
      -DarchetypeVersion=0.11.0 \
      -DgroupId=io.helidon.examples \
      -DartifactId=helidon-sensors \
      -Dpackage=rpi.sensors.mp
```
Notice above:
- the `artifactId`, will generate the code in a directory with that name
- the `package`, where the Java skeletons will be generated

The generated code only contains a simple "Greeting" service. You can run it right now.
The code is generated using `JAX-RS` annotations.

```
 $ cd helidon-sensors
```
then (add proxy parameters if required)
```
 $ mvn package 
```
Eventually you have
```
[INFO] Building jar: target/helidon-sensors.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  21.787 s
[INFO] Finished at: 2019-02-16T08:03:13-08:00
[INFO] ------------------------------------------------------------------------
$
```
At this point, it's ready to run:
```
 $ java -jar target/helidon-sensors.jar
 2019.02.16 08:06:02 INFO org.jboss.weld.Version !thread!: WELD-000900: 3.0.3 (Final)
 2019.02.16 08:06:02 INFO org.jboss.weld.Bootstrap !thread!: WELD-ENV-000020: Using jandex for bean discovery
 2019.02.16 08:06:03 INFO org.jboss.weld.Bootstrap !thread!: WELD-000101: Transactional services not available. Injection of @Inject UserTransaction not available. Transactional observers will be invoked synchronously.
 2019.02.16 08:06:03 INFO org.jboss.weld.Event !thread!: WELD-000411: Observer method [BackedAnnotatedMethod] private org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider.processAnnotatedType(@Observes ProcessAnnotatedType) receives events for all annotated types. Consider restricting events using @WithAnnotations or a generic type with bounds.
 2019.02.16 08:06:03 WARN org.jboss.weld.Bootstrap !thread!: WELD-000146: BeforeBeanDiscovery.addAnnotatedType(AnnotatedType<?>) used for class org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider$JaxRsParamProducer is deprecated from CDI 1.1!
 WARNING: An illegal reflective access operation has occurred
 WARNING: Illegal reflective access by org.jboss.classfilewriter.ClassFile$1 (file:/Users/olediour/repos/raspberry-pi4j-samples/Project.Trunk/REST.clients/REST.assembler/helidon-sensors/target/libs/jboss-classfilewriter-1.2.1.Final.jar) to method java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int)
 WARNING: Please consider reporting this to the maintainers of org.jboss.classfilewriter.ClassFile$1
 WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
 WARNING: All illegal access operations will be denied in a future release
 2019.02.16 08:06:04 INFO org.jboss.weld.Bootstrap !thread!: WELD-ENV-002003: Weld SE container 8b62d29e-5ca4-426a-b089-a74267218f50 initialized
 2019.02.16 08:06:04 INFO io.helidon.microprofile.security.SecurityMpService !thread!: Security extension for microprofile is enabled, yet security configuration is missing from config (requires providers configuration at key security.providers). Security will not have any valid provider.
 2019.02.16 08:06:05 INFO io.helidon.webserver.NettyWebServer !thread!: Channel '@default' started: [id: 0x698b64fc, L:/0:0:0:0:0:0:0:0:8080]
 2019.02.16 08:06:05 INFO io.helidon.microprofile.server.ServerImpl !thread!: Server started on http://localhost:8080 (and all other host addresses) in 203 milliseconds.
 http://localhost:8080/greet
```
Your micro-service is running in `jetty`, on port `8080`. This port is defined in
the generated file `helidon-sensors/src/main/resources/META-INF/microprofile-config.properties` along
with other properties used at runtime.

You can see what the service is providing from any REST client:
```
 $ curl http://192.168.42.8:8080/greet
   {"message":"Hello World!"}
```
It works 👍.

To move beyond, see the [Helidon documentation](https://helidon.io/docs/latest/#/getting-started/02_base-example#Prerequisites).

We will now replace this code with ours.

> Note: if you'd rather use `gradle` than `maven`, to generate the require `build.gradle`, 
> from the directory where the generated `pom.xml` lives, just type
```
 $ ../../../../gradlew init
```

###### Install required dependencies
We will use in our micro service resources from other modules in this project.

We thus need to `install` them in the local Maven repo.

From the directory `I2C.SPI`:
```
 I2C.SPI $ ../gradlew install
 $ cd ../RMI.samples
 $ ../gradlew install
 $ cd ../common-utils
 $ ../gradlew install
 $ 
```

We need to add those dependencies to the `pom.xml`.

First add one repository
```xml
 <repositories>

   <repository>
     <id>sonatype-repo</id>
     <url>https://oss.sonatype.org/content/groups/public/</url>
   </repository>

</repositories>
```
Then at the end of the `<dependencies>` section:
```xml
 <dependency>
   <groupId>oliv.raspi.coffee</groupId>
   <artifactId>I2C.SPI</artifactId>
   <version>1.0</version>
 </dependency>
```

If you want to use `gradle`, in the `build.gradle`, change the following:
```groovy
sourceCompatibility = 1.8
targetCompatibility = 1.8
```
and add in the `repositories`:
```groovy
repositories {
    mavenLocal()
    mavenCentral()
    maven { url "http://repo.maven.apache.org/maven2" }
    maven { url "https://oss.sonatype.org/content/groups/public" }
}
```
and in `dependencies`:
```groovy
dependencies {
    compile 'oliv.raspi.coffee:I2C.SPI:1.0'
```

Now we're ready to dive in the code.


##### fnProject
Still in development, but quite promising. WIP. 🚧

### Using a light custom HTTP Server
Less snappy than `Swagger`, but eventually lighter, in term of footprint.
For small boards (like the Raspberry Pi Zero), this would be my preferred option.

## Run the server


## Build a flow using Node-RED


## Try it!

