package nmea.forwarders;

import nmea.parser.StringParsers;
import util.TextToSpeech;

import java.util.Properties;

/**
 * This is a {@link Forwarder}, forwarding chosen data to a REST server (POST).
 * In this case this is Adafruit.IO
 * <br>
 *   Data are (can be)
 *   - Air Temperature
 *   - Atmospheric pressure
 *   - Humidity
 *   - Wind Speed
 *   - Wind direction
 *   - Rain (precipitation rate)
 * <br>
 * It must be loaded dynamically. As such, it can be set only from the properties file
 * used at startup. It - for now - cannot be managed from the Web UI.
 * The REST api is not aware of it.
 * <br>
 * To load it, use the properties file at startup:
 * <pre>
 *   forward.XX.cls=nmea.forwarders.RESTPublisher
 *   forward.XX.properties=rest.server.properties
 * </pre>
 * A jar containing this class and its dependencies must be available in the classpath.
 *
 * // TODO Logging rate, per channel.
 */
public class RESTPublisher implements Forwarder {
	private double previousTemp = -Double.MAX_VALUE;

	/*
	 * @throws Exception
	 */
	public RESTPublisher() throws Exception {
	}

	@Override
	public void write(byte[] message) {
		String str = new String(message);
//	System.out.println(">>>> Mess:" + str);
		if (StringParsers.validCheckSum(str)) {
//		String deviceId = StringParsers.getDeviceID(str);
			String sentenceId = StringParsers.getSentenceID(str);
			if ("MTA".equals(sentenceId)) {
				double airTemp = StringParsers.parseMTA(str);
				if (Math.abs(airTemp - previousTemp) >= 1.0) {
					previousTemp = airTemp;
					TextToSpeech.speak(String.format("Temperature is now %.1f degrees Celcius.", airTemp));
				}
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing (speaking) to " + this.getClass().getName());
	}

	public static class SpeakerBean {
		private String cls;
		private String type = "thermo-speaker";

		public SpeakerBean(RESTPublisher instance) {
			cls = instance.getClass().getName();
		}
	}

	@Override
	public Object getBean() {
		return new SpeakerBean(this);
	}

	@Override
	public void setProperties(Properties props) {
	}
}
