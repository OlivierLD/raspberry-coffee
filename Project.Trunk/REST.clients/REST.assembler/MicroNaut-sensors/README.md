# MicroNaut, serverless microservices.
Just like before, we want to expose features of the Raspberry Pi as a REST Service.
Here we want to read the ambient light (aka luminosity) from a photocell connected to and 
Analog to Digital Converter (`ADC`), and make it available to any REST client connected on the network the Raspberry Pi runs on.

We use MicroNaut, available at [micronaut.io](https://micronaut.io/), where you'll find all the instructions you need
to install it.

Then, make sure `sdkman` is started:
```bash
$ source "$HOME/.sdkman/bin/sdkman-init.sh"
```
And create your app scaffolding:
```bash
$ mn create-app micronaut.sensors.complete
```
This creates a new java project in a `complete` folder, with a `micronaut.sensors` package.

> Note: From an IDE (like IntelliJ), it's much better to open the `complete` directory as a new Project.
 
Then add a Controller
```java
package micronaut.sensors;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/ambient-light") 
public class SensorsController {
    @Get 
    @Produces(MediaType.APPLICATION_JSON) 
    public String getLuminosity() {
        return "{ \"light\": 23.45 }"; 
    }
}
```
Add a test if needed...

```bash
$ ./gradlew test
$ ./gradlew run
```

```bash
$ curl -X GET http://localhost:8080/ambient-light
{ "light": 23.45 }
$
```

## Docker
```bash
$ ./gradlew clean shadowJar
$ docker build . -t micronaut
$ docker run -p 8080:8080 micronaut
```
- The `-t` tags the image.

And from another shell
```bash
$ curl http://localhost:8080/ambient-light
. . .
```

## Configuration
Your service might need some custom configuration.
This is well documented [here](https://guides.micronaut.io/micronaut-configuration/guide/index.html). 

Here we want to provide the physical number of
the GPIO pins the `ADC` is connected on, `MISO`, `MOSI`, `CLOCK` and `CHIP-SELECT` for the `SPI` interface, `Channel` for the analog data input. 

In the file `resources/application.yml`, add
```yaml
adc:
  clk: 18
  miso: 23
  mosi: 24
  cs: 25
  channel: 2
```
Create an _annotated_ matching bean, next to the Controller
```java
package micronaut.sensors;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("adc")
public class ADCConfiguration {
	private int clk;
	private int miso;
	private int mosi;
	private int cs;
	private int channel;

	public ADCConfiguration() {}

	public int getClk() {
		return clk;
	}

	public void setClk(int clk) {
		this.clk = clk;
	}

	public int getMiso() {
		return miso;
	}

	public void setMiso(int miso) {
		this.miso = miso;
	}

	public int getMosi() {
		return mosi;
	}

	public void setMosi(int mosi) {
		this.mosi = mosi;
	}

	public int getCs() {
		return cs;
	}

	public void setCs(int cs) {
		this.cs = cs;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}
}
```
Modify the Controller, add a constructor, receiving the configuration bean as parameter:
```java
. . .
public class SensorsController {

    private final ADCConfiguration adcConfiguration;

    public SensorsController(@Nullable ADCConfiguration adcConfiguration) {
        this.adcConfiguration = adcConfiguration;
        if (this.adcConfiguration != null) {
            System.out.println(String.format("ADC Config: Channel:%d, MISO:%d, MOSI:%d, CLK:%d, CS:%d",
                this.adcConfiguration.getChannel(),
                this.adcConfiguration.getMiso(),
                this.adcConfiguration.getMosi(),
                this.adcConfiguration.getClk(),
                this.adcConfiguration.getCs()));
        }
    }

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public String getLuminosity() {
. . .
```
Build and run, the first invocation will show the configuration parameters as expected above,
as stored in the `yaml` document:
```
> Task :run
OpenJDK 64-Bit GraalVM CE 19.3.0 warning: forcing TieredStopAtLevel to full optimization because JVMCI is enabled
07:05:34.029 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 1571ms. Server Running: http://localhost:8080
ADC Config: Channel:2, MISO:23, MOSI:24, CLK:18, CS:25
<=========----> 75% EXECUTING [4m 13s]
```

## Dependencies
This project will require the functionalities provided by the `ADC` module,
in the `raspberry-coffee` project.

From the ADC module, make sure you `install` it in your local Maven repo:
```bash
ADC$ ../gradlew clean install
```

From the MicroNaut service project, add the dependency on ADC in `build.gradle`:
```groovy
dependencies {
  . . .
  compile 'oliv.raspi.coffee:ADC:1.0'
  . . .
}
```
Make sure `mavenLocal()` is in the repositories:
```groovy
repositories {
    mavenCentral()
    mavenLocal()
    maven { url "https://jcenter.bintray.com" }
}
```

Now, create - as featured in this project - the class `rpi.sensors.ADCChannel`, and instantiate it in the Controller's constructor:
```java
@Controller("/ambient-light")
public class SensorsController {

    private final ADCConfiguration adcConfiguration;
    private ADCChannel adcChannel;

    public SensorsController(@Nullable ADCConfiguration adcConfiguration) {
        this.adcConfiguration = adcConfiguration;
        if (this.adcConfiguration != null) {
            System.out.println(String.format("ADC Config: Channel:%d, MISO:%d, MOSI:%d, CLK:%d, CS:%d",
                this.adcConfiguration.getChannel(),
                this.adcConfiguration.getMiso(),
                this.adcConfiguration.getMosi(),
                this.adcConfiguration.getClk(),
                this.adcConfiguration.getCs()));
            this.adcChannel = new ADCChannel(
                this.adcConfiguration.getMiso(),
                this.adcConfiguration.getMosi(),
                this.adcConfiguration.getClk(),
                this.adcConfiguration.getCs(),
                this.adcConfiguration.getChannel());
        }
    }
    . . .
}
```
It can now be invoked by the operation in the service:
```java
    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public String getLuminosity() {
        float volume = 0;
        if (this.adcChannel != null) {
            volume = this.adcChannel.readChannelVolume();
        }
        return String.format("{ \"light\": %05.02f }", volume);
    }
```

The service is ready to run, reading the luminosity (in `%`, instead of `[0..1023]`) from the ADC.
And the Docker step mentioned above works just the same.

## Next 
- Life cycle management (free resources on close...)
- Debugging

---
