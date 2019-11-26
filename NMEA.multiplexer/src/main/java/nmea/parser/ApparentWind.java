package nmea.parser;

public class ApparentWind
				extends Wind {
	public ApparentWind(int i, double d) {
		super(i, d);
	}

	// @Override
	public static String getCsvHeader(String separator) {
		return String.format("awa%saws", SEP).replace(SEP, separator);
	}

	@Override
	public String getCsvData(String separator) {
		return String.format("%d%s%f",
				this.getAngle(), separator,
				this.getSpeed());
	}
}
