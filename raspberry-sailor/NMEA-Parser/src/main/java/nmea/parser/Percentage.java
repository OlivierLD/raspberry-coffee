package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public abstract class Percentage implements Serializable {
	private double percentage = 0d;
	private static final String PERCENT = "%";
	private static final DecimalFormat FMT = new DecimalFormat("##0.00");

	public Percentage() {
	}

	public Percentage(double p) {
		this.percentage = p;
	}

	public double getValue() {
		return this.percentage;
	}

	public String toString() {
		double d = this.percentage;
		return FMT.format(d) + PERCENT;
	}
}
