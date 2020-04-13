package rest.oas.factories;

import rest.oas.SensorsApiService;
import rest.oas.impl.SensorsApiServiceImpl;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2019-08-23T12:20:17.320-07:00[America/Los_Angeles]")
public class SensorsApiServiceFactory {
    private final static SensorsApiService service = new SensorsApiServiceImpl();

    public static SensorsApiService getSensorsApi() {
        return service;
    }
}
