# Micronaut
Implement a Celestial Almanac as a serverless micro service.

- Use [`SDKMAN`](https://sdkman.io/) to install Micronaut if not there yet
as described [here](https://micronaut-projects.github.io/micronaut-starter/latest/guide/#installation).
    - To start `SdkMan`: `$ source ~/.sdkman/bin/sdkman-init.sh`
    - Then `sdk version`

- See if `micromaut` is available:
    - `$ mn -V`

- Create a new Micronaut application
```
$ mn create-app celestial.almanach.mn
```
<!--    
> Note: From an IDE like IntelliJ, navigate to the newly created `mn/build.gradle`,
> right-click on it, choose `Mark Directory as` > `. . .`. 
-->

- In the package `celestial.lamamanc`, add a controller, `AstroController`:
```java
package celestial.almanach;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/astro")
public class AstroController {
    @Get("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCelestialData() {
        return "{ \"data\": 23.45 }";
    }
}
```
- The service (dummy for now) is ready to run, from the `mn` directory:
```
$ ./gradlew run
```
Then from another terminal:
```
$ curl -X GET http://localhost:8080/astro/data
  { "data": 23.45 }
```

You're all set! Let's flesh it out.

- Add dependencies
    - Publish `common-utils` to your local Maven repo (or anywhere you want, as long as you can reach it):
    ```
     $ cd common-utils
     $ ../gradlew clean install
    ```      
    - Add the dependency in `build.gradle`:
    In the `repositories`, add `mavenLocal()`
    ```
    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }
    ```
    In the `dependencies`:
    ```
    implementation 'oliv.raspi.coffee:common-utils:1.0'
    ```  
> Important: See the `values.deltaT` value in `application.yml`
>  

See in the sources of `AstroController` how the code is implemented to return the expected data.

## For real
Start the service as above:
```
$ ./gradlew run
```
Then from another terminal:
```
$ curl -X GET http://localhost:8080/astro/data \
       -H "year: 2020" \
       -H "month: 3" \
       -H "day: 6" | jq
{
  "saturn": {
    "gha": 91.22096018347229,
    "sd": 7.555608344947186,
    "dec": -19.254628156927264,
    "hp": 0.8031429926926816,
    "ra": 308.1961988913066
  },
  "mars": {
    "gha": 357.72772035841575,
    "sd": 3.809273110745039,
    "dec": 17.46149327922712,
    "hp": 7.1578520803187775,
    "ra": 41.689438716363185
  },
  "moon-phase": 89.85737475226487,
  "jupiter": {
    "gha": 85.86465571771468,
    "sd": 16.230543123339448,
    "dec": -17.996840861765808,
    "hp": 1.4499329157522056,
    "ra": 313.5525033570642
  },
  "polaris": {
    "gha": 354.88847265264127,
    "dec": 89.35706923688379,
    "ra": 44.528686422137646
  },
  "eot": -13.931440556059442,
  "lunar-dist": 89.99875671256869,
  "sun": {
    "gha": 80.8338065276518,
    "sd": 973.3848664764512,
    "dec": -16.00166019290474,
    "hp": 8.920048889461473,
    "ra": 318.5833525471271
  },
  "moon": {
    "gha": 174.95141341289897,
    "sd": 968.0276947239538,
    "dec": -14.061257166571565,
    "hp": 3552.6460157254605,
    "ra": 224.46574566187994
  },
  "venus": {
    "gha": 92.96454946370305,
    "sd": 5.063396661145906,
    "dec": -19.9999683525089,
    "hp": 5.2945909914526865,
    "ra": 306.45260961107584
  },
  "day-of-week": "THU",
  "mean-obliquity-of-ecliptic": 23.436547777657022,
  "context": {
    "month": 2,
    "hour": 17,
    "year": 2021,
    "day": 4,
    "minute": 37,
    "second": 16,
    "delta-t": 71.71293632812495
  },
  "aries-gha": 39.417159074778915
}
```

## Docker
Very easy, just a few clicks away:

From the `mn` directory (make sure proxies are off in Docker's settings):
```
 $ ./gradlew clean shadowJar
 $ docker build . -t almanac
```
then
```
$ docker run --rm -p 8080:8080 almanac
```
... And the exact same `curl` command as before should work!

> Note (Feb-2021): To run this on the Raspberry Pi, you need to tweak the `Dockerfile`:  
> replace
> ```
> FROM openjdk:14-alpine
> ```
> with  
> ```
> FROM debian:buster
> RUN apt-get update
> RUN apt-get install -y openjdk-11-jdk
> ```

To reach a Micronaut server running a Raspberry Pi, try
```
curl -X GET http://192.168.42.42:8080/astro/data \
       -H "year: 2020" \
       -H "month: 3" \
       -H "day: 6" | jq
```
where `192.168.42.42` is the IP address of the Raspberry Pi. 

---
