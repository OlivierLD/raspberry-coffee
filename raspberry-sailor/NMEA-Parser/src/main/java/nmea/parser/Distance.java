package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public class Distance implements NMEADoubleValueHolder, Serializable {
	private double distance = 0d;
	private static final String UNIT = "nm"; // TODO Localize
	private static final DecimalFormat FMT = new DecimalFormat("00.00");

	public Distance() {
	}

	public Distance(double dist) {
		this.distance = dist;
	}

	public double getValue() {
		return this.distance;
	}

	public String toString() {
		return FMT.format(this.distance) + " " + UNIT;
	}

	public void setDoubleValue(double d) {
		this.distance = d;
	}

	public double getDoubleValue() {
		return this.distance;
	}
}
