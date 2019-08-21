package sensors.io.factories;

import sensors.io.SensorsApiService;
import sensors.io.impl.SensorsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2019-08-20T17:28:36.109-07:00[America/Los_Angeles]")public class SensorsApiServiceFactory {
    private final static SensorsApiService service = new SensorsApiServiceImpl();

    public static SensorsApiService getSensorsApi() {
        return service;
    }
}
