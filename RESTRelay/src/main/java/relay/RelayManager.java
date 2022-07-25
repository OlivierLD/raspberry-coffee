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

	private final Map<Integer, Pin> relayMap;

	public RelayManager(Map<Integer, Pin> relayMap) {
		this.relayMap = relayMap;
		try {
			gpio = GpioFactory.getInstance();
		} catch (Throwable t) {
			simulating = true;
			System.out.println("IO Lib not available >> Simulating Relays");
		}

		relayMap.forEach((key, value) -> {
			// For a relay it seems that HIGH means NC (Normally Closed)...
			if (gpio != null) {
				relays.add(gpio.provisionDigitalOutputPin(value, String.valueOf(key), PinState.HIGH));
			}
		});

	}

	public void set(int device, String status) {
		if (!simulating) {
			Optional<GpioPinDigitalOutput> oPin = relays.stream().filter(pinOut -> pinOut.getName().equals(String.valueOf(device))).findFirst();
			if (oPin.isPresent()) {
				GpioPinDigitalOutput pin = oPin.get();
				if ("true".equals(System.getProperty("relay.verbose", "false"))) {
					System.out.printf("Setting Relay#%d %s\n", device, status);
				}
				if ("on".equals(status)) {
					pin.low();
				} else {
					pin.high();
				}
			} else {
				System.out.printf("Relay %d not found...\n", device);
			}
		} else {
			System.out.printf("Setting relay #%d %s\n", device, status);
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
				System.out.printf("Relay %d not found...\n", device);
			}
		} else {
			status = (System.currentTimeMillis() % 2) == 0;
			System.out.printf("Getting status for relay #%d: %s\n", device, status);
		}
		return status;
	}

	public Map<Integer, Pin> getRelayMap() {
		return this.relayMap;
	}

	public void shutdown() {
		if (gpio != null) {
			gpio.shutdown();
		}
	}
}
