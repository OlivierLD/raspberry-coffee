package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public class Angle180LR extends Angle // implements NMEADoubleValueHolder, Serializable
{
	private double angle = 0d;
	private static final String UNIT = "\272";
	private static final DecimalFormat FMT = new DecimalFormat("##0.0");

	public Angle180LR() {
	}

	public Angle180LR(double angle) {
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
		String ret = FMT.format(d) + UNIT;
		if (d > 0 && d < 180) // Right
			ret = "R " + FMT.format(d) + UNIT;
		if (d > 180 && d < 360) // Left
			ret = "L " + FMT.format(360d - d) + UNIT;
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
