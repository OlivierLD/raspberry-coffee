package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public class Angle180EW extends Angle // implements NMEADoubleValueHolder, Serializable
{
	private double angle = 0d;
	private static final String UNIT = "\272";
	private static final DecimalFormat FMT = new DecimalFormat("##0.0");

	public Angle180EW() {
	}

	public Angle180EW(double angle) {
		this.angle = angle;
		if (Math.abs(angle) > 180)
			angle -= 360;
	}

	public double getValue() {
		return this.angle;
	}

	public String toString() {
		return toFormattedString() + (this.angle != -Double.MAX_VALUE ? " (" + FMT.format(this.angle) + ")" : "");
	}

	public String toFormattedString() {
		String ret = "";
		double d = this.angle;
		if (d == -Double.MAX_VALUE)
			ret = " - ";
		else {
			while (d < 0)
				d += 360d;
			while (d > 360)
				d -= 360d;
			ret = FMT.format(d) + UNIT;
			if (d > 0 && d < 180) // E
				ret = "E " + FMT.format(d) + UNIT;
			if (d > 180 && d < 360) // W
				ret = "W " + FMT.format(360d - d) + UNIT;
		}
		return ret;
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
