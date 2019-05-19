package nmea.parser;

import java.io.Serializable;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

public class UTCHolder
				implements Serializable {
	private UTCDate utcDate = null;
	private UTCTime utcTime = null;
	private static SimpleDateFormat FMT = new SimpleDateFormat("EEE, yyyy MMM dd HH:mm:ss 'UTC'");

	static {
		FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	public UTCHolder(UTCDate d) {
		super();
		this.utcDate = d;
	}

	public UTCHolder(UTCTime t) {
		super();
		this.utcTime = t;
	}

	public Date getValue() {
		Date d = null;
		if (this.utcDate != null)
			d = this.utcDate.getValue();
		else
			d = this.utcTime.getValue();
		return d;
	}

	public boolean isNull() {
		return (this.utcDate == null && this.utcTime == null);
	}

	public String toString() {
		Date date = getValue();
		return (date != null) ? FMT.format(date) : null;
	}
}
