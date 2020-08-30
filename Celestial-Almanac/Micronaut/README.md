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
$ curl -X GET http://localhost:8080/astro/data | jq
{
  "saturn": {
    "gha": 302.6063741637422,
    "sd": 8.97397615277244,
    "dec": -21.210248264375853,
    "hp": 0.9539120789010135,
    "ra": 298.12900867232213
  },
  "mars": {
    "gha": 213.90811520609913,
    "sd": 9.272124072158014,
    "dec": 6.527043955608099,
    "hp": 17.422875874050767,
    "ra": 26.827267629965167
  },
  "moon-phase": " +gib",
  "jupiter": {
    "gha": 311.4630646607191,
    "sd": 22.265844615064644,
    "dec": -22.665415705001337,
    "hp": 1.9890881505981155,
    "ra": 289.2723181753452
  },
  "polaris": {
    "gha": 196.19223069831045,
    "dec": 89.34471823341387,
    "ra": 44.543152137753886
  },
  "eot": -0.720016882798518,
  "lunar-dist": 139.28713475003428,
  "sun": {
    "gha": 82.17832911263369,
    "sd": 950.3831483097083,
    "dec": 9.005068065641636,
    "hp": 8.709262326350338,
    "ra": 158.55705372343064
  },
  "moon": {
    "gha": 302.048049392692,
    "sd": 924.0993087109812,
    "dec": -23.35366791464988,
    "hp": 3391.415777616733,
    "ra": 298.6873334433723
  },
  "venus": {
    "gha": 127.44051578331904,
    "sd": 10.045100807385728,
    "dec": 19.596589295857335,
    "hp": 10.503759393596921,
    "ra": 113.29486705274529
  },
  "day-of-week": "SAT",
  "mean-obliquity-of-ecliptic": 23.436604389129297,
  "context": {
    "month": 8,
    "hour": 17,
    "year": 2020,
    "day": 29,
    "minute": 29,
    "second": 26,
    "delta-t": 69.2201
  },
  "aries-gha": 240.73538283606433
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
... And the same `curl` as before should work!
