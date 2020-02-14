# MicroNaut, serverless microservices.
Available at [micronaut.io](https://micronaut.io/)

```bash
$ source "$HOME/.sdkman/bin/sdkman-init.sh"
$ mn create-app micronaut.sensors.complete
```
This creates a new java project in a `complete` folder, with a `micronaut.sensors` package. 
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
    public String index() {
        return "{ \"light\": 23.45 }"; 
    }
}
```
Add a test if needed...

In `build.gradle`, add dependencies on the required project (`oliv.raspi.coffee, ADC, 1.0`) (after running the required `./gradlew install` commands...)
```bash
$ ./gradlew test
$ ./gradlew run
```

```bash
$ curl -X GET http://localhost:8080/light
```

## Next
Docker

---
