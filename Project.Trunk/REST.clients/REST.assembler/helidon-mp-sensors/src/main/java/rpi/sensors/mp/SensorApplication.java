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

import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.event.Observes;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.helidon.common.CollectionsHelper;

/**
 * Simple Application that produces a greeting message.
 */
@ApplicationScoped
@ApplicationPath("/v1")
public class SensorApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return CollectionsHelper.setOf(SensorResource.class);
    }


    public void onShutdown(@Observes @BeforeDestroyed(ApplicationScoped.class) Object event) {
        System.out.println(String.format("\nStopping the server, obj is a %s", event.getClass().getName()));
        Set<Class<?>> classes = this.getClasses();
        System.out.println(String.format("Classes: %d", classes.size()));
        classes.forEach(cls -> {
            System.out.println("=> " + cls.getName());
        });

        Set<Object> singletons = this.getSingletons();
        System.out.println(String.format("Singletons: %d", singletons.size()));
        singletons.forEach(obj -> {
            System.out.println(String.format("Singleton: %s", obj.getClass().getName()));
        });

        Map<String, Object> properties = this.getProperties();
        System.out.println(String.format("Properties: %d", properties.size()));
        properties.keySet().forEach(key -> {
            System.out.println(String.format("%d = %d", key, properties.get(key)));
        });
    }

}
