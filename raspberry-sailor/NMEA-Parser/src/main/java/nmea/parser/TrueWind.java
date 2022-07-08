package nmea.parser;

public class TrueWind
		extends Wind {
	public TrueWind(int i, double d) {
		super(i, d);
	}

	// @Override
	public static String getCsvHeader(String separator) {
		return String.format("twa%stws", SEP).replace(SEP, separator);
	}

	@Override
	public String getCsvData(String separator) {
		return String.format("%d%s%f",
				this.getAngle(), separator,
				this.getSpeed());
	}
}
