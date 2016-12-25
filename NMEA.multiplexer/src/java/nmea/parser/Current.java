package nmea.parser;

import java.io.Serializable;

public class Current implements Serializable {
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
}
