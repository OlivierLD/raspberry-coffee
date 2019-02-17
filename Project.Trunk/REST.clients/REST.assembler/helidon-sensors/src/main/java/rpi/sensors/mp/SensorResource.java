/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rpi.sensors.mp;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Read sensors, drive a relay
 */
@Path("/sensors")
@RequestScoped
public class SensorResource {

    /**
     * The greeting message provider.
     */
    private final SensorProvider sensorProvider;

    /**
     * Using constructor injection to get a configuration property.
     * By default this gets the value from META-INF/microprofile-config
     *
     * @param sensorProvider
     */
    @Inject
    public SensorResource(SensorProvider sensorProvider) {
        this.sensorProvider = sensorProvider;
    }

    /**
     * Return the pump status
     *
     * @return {@link SensorProvider.RelayStatus}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Path("/relay")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorProvider.RelayStatus getRelayStatus() {

        return this.sensorProvider.getRelayStatus();
    }

    /**
     * Set the pump on or off
     *
     * @param relayStatus
     * @return {@link SensorProvider.RelayStatus}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Path("/relay")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SensorProvider.RelayStatus setRelayStatus(SensorProvider.RelayStatus relayStatus) {

        return this.sensorProvider.setRelayStatus(relayStatus);
    }

    /**
     * Return the pump status
     *
     * @return {@link SensorProvider.RelayStatus}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Path("/ambient-light")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorProvider.AmbientLight readAmbientLight() {

        return this.sensorProvider.getAmbientLight();
    }

}
