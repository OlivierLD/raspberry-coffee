package nmea.parser;

import nmea.utils.WindUtils;

public class TrueWindDirection
				extends Angle360 {
	public TrueWindDirection(double d) {
		super(d);
	}

	public TrueWindDirection() {
		super();
	}

	@Override
	public String toString() {
		return super.toString() + " (" + WindUtils.getRoseDir(angle) + ")";
	}
}
