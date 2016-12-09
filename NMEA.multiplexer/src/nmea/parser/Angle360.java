package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public class Angle360 extends Angle // implements NMEADoubleValueHolder, Serializable
{
	protected double angle = 0d;
	protected static final String UNIT = "\272";
	protected static final DecimalFormat FMT = new DecimalFormat("000.0");

	public Angle360() {
	}

	public Angle360(double angle) {
		this.angle = angle;
	}

	public double getValue() {
		return this.angle;
	}

	public String toString() {
		double d = this.angle;
		while (d < 0) d += 360d;
		while (d > 360) d -= 360d;
		return FMT.format(d) + UNIT + "  (" + FMT.format(this.angle) + ")";
	}

	public void setDoubleValue(double d) {
		this.angle = d;
	}

	public double getDoubleValue() {
		return angle;
	}
}
