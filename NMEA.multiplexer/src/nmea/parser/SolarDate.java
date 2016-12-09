package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

public class SolarDate implements Serializable {
	private Date date = null;
	private static SimpleDateFormat FMT = new SimpleDateFormat("EEE, yyyy MMM dd HH:mm:ss");

	static {
		FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	public SolarDate() {
	}

	public SolarDate(Date date) {
		this.date = date;
	}

	public Date getValue() {
		return this.date;
	}

	public String toString() {
		return FMT.format(this.date);
	}
}
