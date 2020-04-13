package nmea.parser;

import java.io.Serializable;

public class HDG extends NMEAComposite implements Serializable {

	private double heading = 0d;
	private double deviation = 0d;
	private double variation = 0d;

	public HDG heading(double heading) {
		this.heading = heading;
		return this;
	}

	public HDG deviation(double deviation) {
		this.deviation = deviation;
		return this;
	}

	public HDG variation(double variation) {
		this.variation = variation;
		return this;
	}

	public double getHeading() {
		return heading;
	}

	public double getDeviation() {
		return deviation;
	}

	public double getVariation() {
		return variation;
	}

	// @Override
	public static String getCsvHeader(String separator) {
		return String.format("heading%sdeviation%svariation", SEP, SEP).replace(SEP, separator);
	}

	@Override
	public String getCsvData(String separator) {
		return String.format("%f%s%f%s%f",
				heading, separator,
				deviation, separator,
				variation);
	}

	@Override
	public String toString() {
		return String.format("Heading: %d, deviation: %.02f, variation: %.02f", (int)Math.round(heading), deviation, variation);
	}
}
