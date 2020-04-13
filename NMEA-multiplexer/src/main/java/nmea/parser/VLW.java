package nmea.parser;

import java.io.Serializable;

public class VLW extends NMEAComposite implements Serializable {

	private double log = 0d;
	private double daily = 0d;

	public VLW log(double log) {
		this.log = log;
		return this;
	}

	public VLW daily(double daily) {
		this.daily = daily;
		return this;
	}

	public double getLog() {
		return log;
	}

	public double getDaily() {
		return daily;
	}

	// @Override
	public static String getCsvHeader(String separator) {
		return String.format("log%sdaily", SEP).replace(SEP, separator);
	}

	@Override
	public String getCsvData(String separator) {
		return String.format("%f%s%f%",
				log, separator,
				daily);
	}

	@Override
	public String toString() {
		return String.format("LOG: %.02f, Daily: %.02f", log, daily);
	}
}
