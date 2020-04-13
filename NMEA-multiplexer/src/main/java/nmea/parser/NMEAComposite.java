package nmea.parser;

public abstract class NMEAComposite {
	public final static String SEP = "SEP";

	public static String getCsvHeader(String separator) { return "".replace(SEP, separator); }
	public abstract String getCsvData(String separator);
}
