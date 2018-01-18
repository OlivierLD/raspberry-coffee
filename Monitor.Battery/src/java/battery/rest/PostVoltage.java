package battery.rest;

import adafruit.io.rest.HttpClient;
import adc.ADCObserver;
import adc.sample.BatteryMonitor;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import static utils.StaticUtil.userInput;

public class PostVoltage {

	private static String key = "";

	private String BATTERY_FEED = "battery-pi";
	private static boolean DEBUG = true;

	private BatteryMonitor batteryMonitor = null;

	public BatteryMonitor getBatteryMonitor() {
		return batteryMonitor;
	}

	public void setBatteryMonitor(BatteryMonitor batteryMonitor) {
		this.batteryMonitor = batteryMonitor;
	}

	private float voltage = 0f;

	public void consumer(BatteryMonitor.ADCData adcData) {
		this.voltage = adcData.getVoltage();
		if (DEBUG) {
			System.out.println(
							String.format("From ADC Observer: volume %d, value %d, voltage %f",
											adcData.getVolume(),
											adcData.getNewValue(),
											adcData.getVoltage()));
		}
	}

	public float getVoltage() {
		return this.voltage;
	}

	public void setVoltage(String key, float voltage) throws Exception {
		String url = "https://io.adafruit.com/api/feeds/" + BATTERY_FEED + "/data";
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-AIO-Key", key);
		JSONObject value = new JSONObject();
		value.put("value", voltage);
	  System.out.println("Sending " + value.toString(2));
		int httpCode = HttpClient.doPost(url, headers, value.toString());
		if (DEBUG)
			System.out.println("POST Status:" + httpCode);
	}

	public PostVoltage() {
	}

	public static void main(String... args) {

		PostVoltage.key = System.getProperty("aio.key", "").trim();

		if (PostVoltage.key.trim().isEmpty()) {
			throw new RuntimeException("Require the key as System variables (-Daio.key)");
		}

		try {
			PostVoltage postVoltage = new PostVoltage();
			Thread batteryThread = new Thread(() -> {
				try {
					if (DEBUG) {
						System.out.println("Creating BatteryMonitor...");
					}
					BatteryMonitor batteryMonitor = new BatteryMonitor(ADCObserver.MCP3008_input_channels.CH0.ch(), postVoltage::consumer);
					postVoltage.setBatteryMonitor(batteryMonitor);
					if (DEBUG) {
						System.out.println("Creating BatteryMonitor: done");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});
			batteryThread.start();

			System.out.println("Hit [Return] to post the voltage, Q to exit.");
			boolean go = true;
			while (go) {
				String str = userInput("Voltage > ");
				if ("Q".equalsIgnoreCase(str)) {
					go = false;
					System.out.println("Bye.");
				} else {
					float data = postVoltage.getVoltage();
					postVoltage.setVoltage(PostVoltage.key, data);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
