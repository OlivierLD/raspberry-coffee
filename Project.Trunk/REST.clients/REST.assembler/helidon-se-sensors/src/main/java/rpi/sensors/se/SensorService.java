/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
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

package rpi.sensors.se;

import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import rpi.sensors.ADCChannel;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import java.util.Collections;

/**
 * Reads the photo-resistor (aka light sensor)
 * The message is returned as a JSON object
 */

public class SensorService implements Service {

    private ADCChannel adcChannel;

    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    SensorService(Config config) {
        this.adcChannel = new ADCChannel(
            config.get("adc.miso").asInt().orElse(23),
            config.get("adc.mosi").asInt().orElse(25),
            config.get("adc.clk").asInt().orElse(18),
            config.get("adc.cs").asInt().orElse(25),
            config.get("adc.channel").asInt().orElse(0));
    }

    /**
     * A service registers itself by updating the routine rules.
     * @param rules the routing rules.
     */
    @Override
    public void update(Routing.Rules rules) {
        rules
            .get("/ambient-light", this::getAmbientLightVolume);
    }

    /**
     * Return a Ambient Light fro sensor
     * @param request the server request
     * @param response the server response
     */
    private void getAmbientLightVolume(ServerRequest request,
                                       ServerResponse response) {
        sendResponse(response, adcChannel.readChannelVolume());
    }

    private void sendResponse(ServerResponse response, float volume) {
        JsonObject returnObject = JSON.createObjectBuilder()
                .add("light", volume)
                .build();
        response.send(returnObject);
    }

}
