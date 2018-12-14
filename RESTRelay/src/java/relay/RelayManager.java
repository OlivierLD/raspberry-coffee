package relay;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RelayManager {

	private boolean simulating = false;

	private GpioController gpio = null;
	private final List<GpioPinDigitalOutput> relays = new ArrayList<>();

	private Map<Integer, Pin> relayMap;

	public RelayManager(Map<Integer, Pin> relayMap) {
		this.relayMap = relayMap;
		try {
			gpio = GpioFactory.getInstance();
		} catch (Throwable t) {
			simulating = true;
			System.out.println("Simulating Relays");
		}

		relayMap.entrySet().forEach(entry -> {
			// For a relay it seems that HIGH means NC (Normally Closed)...
			if (gpio != null) {
				relays.add(gpio.provisionDigitalOutputPin(entry.getValue(), String.valueOf(entry.getKey()), PinState.HIGH));
			}
		});

	}

	public void set(int device, String status) {
		if (!simulating) {
			Optional<GpioPinDigitalOutput> oPin = relays.stream().filter(pinOut -> pinOut.getName().equals(String.valueOf(device))).findFirst();
			if (oPin.isPresent()) {
				GpioPinDigitalOutput pin = oPin.get();
				if ("true".equals(System.getProperty("relay.verbose", "false"))) {
					System.out.println(String.format("Setting Relay#%d %s", device, status));
				}
				if ("on".equals(status)) {
					pin.low();
				} else {
					pin.high();
				}
			} else {
				System.out.println(String.format("Relay %d not found...", device));
			}
		} else {
			System.out.println(String.format("Setting relay #%d %s", device, status));
		}
	}

	public boolean get(int device) {
		boolean status = false;
		if (!simulating) {
			Optional<GpioPinDigitalOutput> oPin = relays.stream().filter(pinOut -> pinOut.getName().equals(String.valueOf(device))).findFirst(); // ("01".equals(device) ? pin17 : pin18);
			if (oPin.isPresent()) {
				GpioPinDigitalOutput pin = oPin.get();
				status = pin.isLow();
			} else {
				System.out.println(String.format("Relay %d not found...", device));
			}
		} else {
			status = (System.currentTimeMillis() % 2) == 0;
			System.out.println(String.format("Getting status for relay #%d: %s", device, status));
		}
		return status;
	}

	public void shutdown() {
		if (gpio != null) {
			gpio.shutdown();
		}
	}
}
