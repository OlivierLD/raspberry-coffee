package rest.oas.impl;

import rest.oas.*;
import org.openapitools.model.*;

import com.sun.jersey.multipart.FormDataParam;

import org.openapitools.model.AmbientLight;
import org.openapitools.model.RelayStatus;

import java.util.List;
import rest.oas.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2019-08-23T12:20:17.320-07:00[America/Los_Angeles]")
public class SensorsApiServiceImpl extends SensorsApiService {
    @Override
    public Response getRelayStatus(SecurityContext securityContext, Application app, ServletContext context, ServletConfig config, HttpHeaders headers, UriInfo uriInfo)
    throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response readAmbientLight(SecurityContext securityContext, Application app, ServletContext context, ServletConfig config, HttpHeaders headers, UriInfo uriInfo)
    throws NotFoundException {
        // do some magic!
        ADCChannel adcChannel = (ADCChannel)context.getAttribute("adc-chaannel");
        float ambientLight = adcChannel.readChannelVolume();
        SensorData.AmbientLight sensorData = new SensorData.AmbientLight();
        sensorData.setLight(ambientLight);
//        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
        return Response.ok().entity(sensorData).build();
    }
    @Override
    public Response setRelayStatus(RelayStatus relayStatus, SecurityContext securityContext, Application app, ServletContext context, ServletConfig config, HttpHeaders headers, UriInfo uriInfo)
    throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
