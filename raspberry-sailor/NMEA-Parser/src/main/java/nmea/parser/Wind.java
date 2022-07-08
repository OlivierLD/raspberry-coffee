package nmea.parser;

import java.io.Serializable;

public abstract class Wind extends NMEAComposite implements Serializable {
	private double speed = 0.0;
	private int angle = 0;

	public Wind(int a,
	            double s) {
		this.speed = s;
		this.angle = a;
	}

	public double getSpeed() {
		return speed;
	}

	public int getAngle() {
		return angle;
	}

	public String toString() {
		return Double.toString(speed) + " kts, " + Integer.toString(angle) + " deg.";
	}

	// @Override
	public static String getCsvHeader(String separator) {
		return String.format("wa%sws", SEP).replace(SEP, separator);
	}

	@Override
	public String getCsvData(String separator) {
		return String.format("%d%s%f",
				this.angle, separator,
				this.speed);
	}
}
