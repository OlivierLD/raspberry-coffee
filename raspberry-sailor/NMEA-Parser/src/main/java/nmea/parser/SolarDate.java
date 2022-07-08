package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

public class SolarDate implements Serializable {

	private Date date = null;
	private FmtDate fmtDate = null;

	public final static SimpleDateFormat FMT = new SimpleDateFormat("EEE, yyyy MMM dd HH:mm:ss");

	static {
		FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	public SolarDate() {
	}

	public SolarDate(Date date) {
		this.date = date;

		String[] sol = FmtDate.SDF_ARRAY.format(date).split(";");
		this.fmtDate = new FmtDate()
				.epoch(date.getTime())
				.year(Integer.parseInt(sol[0]))
				.month(Integer.parseInt(sol[1]))
				.day(Integer.parseInt(sol[2]))
				.hour(Integer.parseInt(sol[3]))
				.min(Integer.parseInt(sol[4]))
				.sec(Integer.parseInt(sol[5]));
	}

	public Date getValue() {
		return this.date;
	}

	public String toString() {
		return FMT.format(this.date);
	}
}
