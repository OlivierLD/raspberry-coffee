package loggers;

public class LogData {
	// Those are the Adafruit-IO feed names
	private static String AIR_TEMP = "air-temperature";
	private static String HUMIDITY = "humidity";
	private static String SOIL_HUMIDITY = "soil-humidity";
	private static String LAST_WATERING = "last-watering";

	public enum FEEDS {
		AIR(AIR_TEMP),
		HUM(HUMIDITY),
		SOIL(SOIL_HUMIDITY),
		LAST_TIME(LAST_WATERING);

		private String value;

		FEEDS(String value) {
			this.value = value;
		}
		public String value() {
			return this.value;
		}
	}

	private double numValue;
	private String strValue;
	private FEEDS feed;

	public LogData() {
	}

	public LogData numValue(double numValue) {
		this.numValue = numValue;
		return this;
	}

	/**
	 * Feed as a string
	 * @param strValue String, bust must contain a numerical value
	 * @return
	 */
	public LogData strValue(String strValue) {
		this.strValue = strValue;
		return this;
	}

	public LogData feed(FEEDS feed) {
		this.feed = feed;
		return this;
	}

	public FEEDS feed() {
		return this.feed;
	}

	public double numValue() {
		return this.numValue;
	}
	public String strValue() {
		return this.strValue;
	}

	public static FEEDS getFeedByName(String feedName, FEEDS defaultFeed) {
		for (FEEDS feed : FEEDS.values()) {
			if (feed.value().equals(feedName)) {
				return feed;
			}
		}
		return defaultFeed;
	}
}
