package nmea.parser;

import java.io.Serializable;

public class Current extends NMEAComposite implements Serializable {
	public double speed = 0.0;
	public int angle = 0;

	public Current(int a,
	               double s) {
		this.speed = s;
		this.angle = a;
	}

	public String toString() {
		return Double.toString(speed) + " kts, " + Integer.toString(angle) + " deg.";
	}

	// @Override
	public static String getCsvHeader(String separator) {
		return String.format("cur-speed%scur-dir", SEP).replace(SEP, separator);
	}

	@Override
	public String getCsvData(String separator) {
		return String.format("%f%s%d",
				this.speed, separator,
				this.angle);
	}
}
