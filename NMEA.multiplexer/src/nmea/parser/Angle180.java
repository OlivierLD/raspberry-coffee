package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public class Angle180 extends Angle // implements NMEADoubleValueHolder, Serializable
{
	private double angle = 0d;
	//private static final String UNIT = "\272";
	private static final DecimalFormat FMT = new DecimalFormat("000");

	public Angle180() {
	}

	public Angle180(double angle) {
		this.angle = angle;
		if (Math.abs(angle) > 180)
			angle -= 360;
	}

	public double getValue() {
		return this.angle;
	}

	public String toString() {
		double d = this.angle;
		while (d < 0)
			d += 360d;
		while (d > 360)
			d -= 360d;
		String ret = FMT.format(d);
		if (d > 0 && d < 180)   // Right
			ret = FMT.format(d) + " -";
		if (d > 180 && d < 360) // Left
			ret = "- " + FMT.format(360d - d);
		return ret + "  (" + FMT.format(this.angle) + ")";
	}

	public void setDoubleValue(double d) {
		angle = d;
		if (Math.abs(angle) > 180)
			angle -= 360;
	}

	public double getDoubleValue() {
		return angle;
	}
}
