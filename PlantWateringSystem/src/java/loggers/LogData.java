package loggers;

public class LogData {
	private static String AIR_TEMP = "air-temperature";
	private static String HUMIDITY = "humidity";

	public enum FEEDS {
		AIR(AIR_TEMP),
		HUM(HUMIDITY);

		private String value;

		FEEDS(String value) {
			this.value = value;
		}
		public String value() {
			return this.value;
		}
	}

	private double value;
	private FEEDS feed;

	public LogData() {
	}

	public LogData value(double value) {
		this.value = value;
		return this;
	}

	public LogData feed(FEEDS feed) {
		this.feed = feed;
		return this;
	}

	public FEEDS feed() {
		return this.feed;
	}

	public double value() {
		return this.value;
	}
}
