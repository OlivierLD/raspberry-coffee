package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public class Speed implements NMEADoubleValueHolder, Serializable {
	protected double speed = -Double.MAX_VALUE;
	protected final static String UNIT = "kts"; // TODO Localize
	protected final static DecimalFormat FMT = new DecimalFormat("00.00");

	public Speed() {
	}

	public Speed(double speed) {
		this.speed = speed;
	}

	public double getValue() {
		return this.speed;
	}

	public String toString() {
		return FMT.format(this.speed) + " " + UNIT;
	}

	public void setDoubleValue(double d) {
		this.speed = d;
	}

	public double getDoubleValue() {
		return this.speed;
	}
}
