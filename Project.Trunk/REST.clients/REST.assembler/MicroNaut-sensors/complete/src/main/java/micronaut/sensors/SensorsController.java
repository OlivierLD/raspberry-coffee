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

