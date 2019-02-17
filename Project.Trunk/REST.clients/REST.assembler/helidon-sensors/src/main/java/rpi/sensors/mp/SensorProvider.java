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

//import i2c.sensor.BME280;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import rpi.sensors.ADCChannel;
import rpi.sensors.RelayManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provider for greeting message.
 */
@ApplicationScoped
public class SensorProvider {
	private final AtomicReference<String> relayMap = new AtomicReference<>();
	private final AtomicReference<Integer> clk = new AtomicReference<>(18);
	private final AtomicReference<Integer> miso = new AtomicReference<>(23);
	private final AtomicReference<Integer> mosi = new AtomicReference<>(24);
	private final AtomicReference<Integer> cs = new AtomicReference<>(25);
	private final AtomicReference<Integer> channel = new AtomicReference<>(0);

	private RelayManager relayManager;
	private ADCChannel adcChannel;
	/**
	 * Create a new greeting provider, reading the message from configuration.
	 *
	 * relay.map=1,11
	 * #
	 * adc.clk=18
	 * adc.miso=23
	 * adc.mosi=24
	 * adc.cs=25
	 * adc.channel=2
	 *
	 * @param relayMap
	 */
	@Inject
	public SensorProvider(@ConfigProperty(name = "relay.map") String relayMap,
	                      @ConfigProperty(name = "adc.clk") String clk,
	                      @ConfigProperty(name = "adc.miso") String miso,
	                      @ConfigProperty(name = "adc.mosi") String mosi,
	                      @ConfigProperty(name = "adc.cs") String cs,
	                      @ConfigProperty(name = "adc.channel") String channel) {

		this.setRelayMap(relayMap);
		try {
			int pin = Integer.parseInt(clk);
			this.clk.set(pin);
		} catch (NumberFormatException nfe) {
			System.out.println(String.format("Bad CLK pin number [%s], using default %d", clk, this.clk.get()));
		}
		try {
			int pin = Integer.parseInt(miso);
			this.miso.set(pin);
		} catch (NumberFormatException nfe) {
			System.out.println(String.format("Bad MISO pin number [%s], using default %d", miso, this.miso.get()));
		}
		try {
			int pin = Integer.parseInt(mosi);
			this.mosi.set(pin);
		} catch (NumberFormatException nfe) {
			System.out.println(String.format("Bad MOSI pin number [%s], using default %d", mosi, this.mosi.get()));
		}
		try {
			int pin = Integer.parseInt(cs);
			this.cs.set(pin);
		} catch (NumberFormatException nfe) {
			System.out.println(String.format("Bad CS pin number [%s], using default %d", cs, this.cs.get()));
		}
		try {
			int pin = Integer.parseInt(channel);
			this.channel.set(pin);
		} catch (NumberFormatException nfe) {
			System.out.println(String.format("Bad CHANNEL pin number [%s], using default %d", channel, this.channel.get()));
		}

		this.relayManager = new RelayManager(getRelayMap());
		this.adcChannel = new ADCChannel(
				this.miso.get(),
				this.mosi.get(),
				this.clk.get(),
				this.cs.get(),
				this.channel.get());
	}

	public RelayStatus getRelayStatus() {
		RelayStatus status = new RelayStatus();
		// Get the actual relay status
		status.setStatus(this.relayManager.get(1));
		return status;
	}

	public RelayStatus setRelayStatus(RelayStatus status) {
		this.relayManager.set(1, status.isStatus() ? "on" : "off");
		return status;
	}

	public AmbientLight getAmbientLight() {
		float lightPercent = this.adcChannel.readChannelVolume();
		AmbientLight ambientLight = new AmbientLight();
		ambientLight.setLight(lightPercent);
		return ambientLight;
	}

	String getRelayMap() {
		return relayMap.get();
	}

	void setRelayMap(String map) {
		this.relayMap.set(map);
	}

	public static class RelayStatus {
		boolean status = false;

		public RelayStatus() {
		}

		public boolean isStatus() {
			return status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}
	}

	public static class AmbientLight {
		float light;

		public AmbientLight() {
		}

		public float getLight() {
			return light;
		}

		public void setLight(float light) {
			this.light = light;
		}
	}

}
