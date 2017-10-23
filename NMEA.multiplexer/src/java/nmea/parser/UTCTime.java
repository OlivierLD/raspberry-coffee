package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

public class UTCTime implements Serializable {
	private Date date = null;
	private FmtDate fmtDate = null;

	private static SimpleDateFormat FMT = new SimpleDateFormat("HH:mm:ss 'UTC'");

	static {
		FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	public UTCTime() {
	}

	public UTCTime(Date date) {
		this.date = date;
		String[] sol = FmtDate.SDF_ARRAY.format(date).split(";");

		this.fmtDate = new FmtDate()
				.hour(Integer.parseInt(sol[3]))
				.min(Integer.parseInt(sol[4]))
				.sec(Integer.parseInt(sol[5]));
	}

	public Date getValue() {
		return this.date;
	}

	public String toString() {
		return (date != null) ? FMT.format(this.date) : null;
	}
}
