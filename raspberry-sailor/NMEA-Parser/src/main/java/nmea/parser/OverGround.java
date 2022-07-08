package nmea.parser;

import java.io.Serializable;

public class OverGround extends NMEAComposite implements Serializable {
	double speed;
	int course;

	public OverGround() {
	}

	public OverGround(double s, int c) {
		speed = s;
		course = c;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getSpeed() {
		return speed;
	}

	public void setCourse(int course) {
		this.course = course;
	}

	public int getCourse() {
		return course;
	}

	public String toString() {
		return Double.toString(speed) + " kts, " + Integer.toString(course) + "T";
	}

	// @Override
	public static String getCsvHeader(String separator) {
		return String.format("sog%scog", SEP).replace(SEP, separator);
	}

	@Override
	public String getCsvData(String separator) {
		return String.format("%f%s%d",
				this.speed, separator,
				this.course);
	}
}
