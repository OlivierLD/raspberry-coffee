package sensors.io.impl;

import sensors.io.*;
import io.swagger.model.*;

import io.swagger.model.AmbientLight;
import io.swagger.model.RelayStatus;

import java.util.Map;
import java.util.List;
import sensors.io.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2019-08-20T17:28:36.109-07:00[America/Los_Angeles]")public class SensorsApiServiceImpl extends SensorsApiService {
    @Override
    public Response getRelayStatus(SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response readAmbientLight(SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response setRelayStatus(RelayStatus body, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
