package nmea.parser;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class FmtDate {

	public final static SimpleDateFormat SDF_ARRAY = new SimpleDateFormat("yyyy;MM;dd;HH;mm;ss");
	static {
		SDF_ARRAY.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	long epoch;
	int year;
	int month; // Jan: 1, Dec: 12
	int day;
	int hour;
	int min;
	int sec;
	String tz;

	public FmtDate epoch(long epoch) {
		this.epoch = epoch;
		return this;
	}
	public FmtDate year(int year) {
		this.year = year;
		return this;
	}
	public FmtDate month(int month) {
		this.month = month;
		return this;
	}
	public FmtDate day(int day) {
		this.day = day;
		return this;
	}
	public FmtDate hour(int hour) {
		this.hour = hour;
		return this;
	}
	public FmtDate min(int min) {
		this.min = min;
		return this;
	}
	public FmtDate sec(int sec) {
		this.sec = sec;
		return this;
	}
	public FmtDate tz(String tz) {
		this.tz = tz;
		return this;
	}
}
