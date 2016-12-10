package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public class Depth implements NMEADoubleValueHolder, Serializable {
	private double depthInMeters = 0d;
	private static final String METERS = " m";
	private static final String FEET = "'";
	private static final String FATHOMS = " f";

	private static final DecimalFormat FMT = new DecimalFormat("##0.0");

	private final static double METERS_TO_FEET = 3.28083;
	private final static double METERS_TO_FATHOMS = METERS_TO_FEET / 6d;

	public Depth() {
	}

	public Depth(double d) {
		this.depthInMeters = d;
	}

	public double getValue() {
		return this.depthInMeters;
	}

	public String toString() {
		double d = this.depthInMeters;
		return FMT.format(d) + METERS + ", " +
						FMT.format(d * METERS_TO_FEET) + FEET + ", " +
						FMT.format(d * METERS_TO_FATHOMS) + FATHOMS;
	}

	public void setDoubleValue(double d) {
		depthInMeters = d;
	}

	public double getDoubleValue() {
		return depthInMeters;
	}
}
