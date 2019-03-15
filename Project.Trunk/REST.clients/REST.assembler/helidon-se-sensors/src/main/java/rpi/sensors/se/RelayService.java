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

import io.helidon.common.http.Http;
import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import rpi.sensors.RelayManager;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import java.util.Collections;

/**
 * The message is returned as a JSON object
 */

public class RelayService implements Service {

    /**
     * The config value for the key {@code greeting}.
     */
    private RelayManager relayManager;
    private int deviceId = 1;
    private boolean relayStatus = false; // Off

    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    RelayService(Config config) {
        this.relayManager = new RelayManager(config.get("relay.map").asString().orElse("1:11")); // TODO get the device ID from the map...
    }

    /**
     * A service registers itself by updating the routine rules.
     * @param rules the routing rules.
     */
    @Override
    public void update(Routing.Rules rules) {
        rules
            .get("/relay", this::getRelayStatus)
            .put("/relay", this::updateRelayStatus);
    }

    /**
     * Return the relay status.
     * @param request the server request
     * @param response the server response
     */
    private void getRelayStatus(ServerRequest request,
                                ServerResponse response) {
        sendResponse(response, this.relayManager.get(this.deviceId));
    }

    private void sendResponse(ServerResponse response, boolean relayStatus) {
        JsonObject returnObject = JSON.createObjectBuilder()
                .add("status", relayStatus)
                .build();
        response.send(returnObject);
    }

    private void updateRelayFromJson(JsonObject jo, ServerResponse response) {

        if (!jo.containsKey("status")) {
            JsonObject jsonErrorObject = JSON.createObjectBuilder()
                    .add("error", "No status provided")
                    .build();
            response.status(Http.Status.BAD_REQUEST_400)
                    .send(jsonErrorObject);
            return;
        }

        relayStatus = jo.getBoolean("status");
        relayManager.set(deviceId, relayStatus ? "on" :"off"); // Hardware interaction
        response.status(Http.Status.OK_200).send(jo);
    }

    /**
     * Set the greeting to use in future messages.
     * @param request the server request
     * @param response the server response
     */
    private void updateRelayStatus(ServerRequest request,
                                   ServerResponse response) {
        request.content().as(JsonObject.class).thenAccept(jo -> updateRelayFromJson(jo, response));
    }
}
