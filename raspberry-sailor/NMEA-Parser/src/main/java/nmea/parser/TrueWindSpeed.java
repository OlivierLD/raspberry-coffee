package nmea.parser;

import nmea.utils.WindUtils;


public class TrueWindSpeed
				extends Speed {
	public TrueWindSpeed(double d) {
		super(d);
	}

	public TrueWindSpeed() {
		super();
	}

	public String toString() {
		return super.toString() + " (" + WindUtils.getBeaufort(this.speed) + " Beaufort)";
	}
}
