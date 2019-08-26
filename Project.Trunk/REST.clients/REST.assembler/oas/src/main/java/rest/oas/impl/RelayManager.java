package rest.oas.impl;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import utils.PinUtil;
import utils.StaticUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RelayManager {

	private boolean simulating = false;

	private GpioController gpio = null;
	private final List<GpioPinDigitalOutput> relays = new ArrayList<>();

	private Map<Integer, Pin> relayMap;

	public RelayManager(String strMap) {
		this(buildRelayMap(strMap));
	}

	public RelayManager(Map<Integer, Pin> relayMap) {
		this.relayMap = relayMap;
		try {
			gpio = GpioFactory.getInstance();
		} catch (Throwable t) {
			simulating = true;
			System.out.println("Simulating Relays");
		}

		relayMap.entrySet().forEach(entry -> {
			// For a relay it seems that HIGH means NC (Normally Closed)... HIGH <=> Button released. LOW <=> Button pushed.
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

	private static Map<Integer, Pin> buildRelayMap(String strMap) {
		Map<Integer, Pin> map = new HashMap<>();
		String[] array = strMap.split(",");
		Arrays.stream(array).forEach(relayPrm -> {
			String[] tuple = relayPrm.split(":");
			if (tuple == null || tuple.length != 2) {
				throw new RuntimeException(String.format("In [%s], bad element [%s]", strMap, relayPrm));
			}
			try {
				int relayNum = Integer.parseInt(tuple[0]);
				int pinNum = Integer.parseInt(tuple[1]);
				Pin physicalNumber = PinUtil.getPinByPhysicalNumber(pinNum);
				if (physicalNumber == null) {
					throw new RuntimeException(String.format("In [%s], element [%s], pin #%d does not exist", strMap, relayPrm, pinNum));
				}
				map.put(relayNum, physicalNumber);
			} catch (NumberFormatException nfe) {
				throw new RuntimeException(String.format("In [%s], element [%s], bad numbers", strMap, relayPrm));
			}
		});

		return map;
	}

	// For manual tests
	public static void main(String... args) {
		// Relay map
		String mapStr = "1:11";
		//               | |
		//               | Physical pin #11 (GPIO_0)
		//               Relay num for this app
		Map<Integer, Pin> relayMap = null;
		try {
			relayMap = buildRelayMap(mapStr);
			if ("true".equals(System.getProperty("relay.verbose", "false"))) {
				relayMap.entrySet().forEach(entry -> {
					System.out.println(String.format("Relay #%d mapped to pin %d (%s) ", entry.getKey(), PinUtil.findByPin(entry.getValue()).pinNumber(), PinUtil.findByPin(entry.getValue()).pinName() ));
				});
			}
		} catch (Exception ex) {
			throw ex;
		}
		RelayManager relayManager = new RelayManager(relayMap);
		System.out.println("Type Q at the prompt to quit");
		boolean keepGoing = true;
		while (keepGoing) {
			String input = StaticUtil.userInput("Q to quit, + to turn ON, - to turn OFF > ");
			if ("q".equalsIgnoreCase(input)) {
				keepGoing = false;
			} else {
				switch (input) {
					case "+":
						relayManager.set(1, "on");
						break;
					case "-":
						relayManager.set(1, "off");
						break;
					default:
						System.out.println(String.format("Unknown Command [%s]", input));
						break;
				}
			}
		}
		relayManager.shutdown();
	}
}
