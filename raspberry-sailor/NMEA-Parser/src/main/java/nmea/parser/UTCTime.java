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
		String[] utc = FmtDate.SDF_ARRAY.format(date).split(";");

		this.fmtDate = new FmtDate()
				.epoch(date.getTime())
				.year(Integer.parseInt(utc[0]))
				.month(Integer.parseInt(utc[1]))
				.day(Integer.parseInt(utc[2]))
				.hour(Integer.parseInt(utc[3]))
				.min(Integer.parseInt(utc[4]))
				.sec(Integer.parseInt(utc[5]));
	}

	public Date getValue() {
		return this.date;
	}

	@Override
	public String toString() {
		return (date != null) ? FMT.format(this.date) : null;
	}
}
