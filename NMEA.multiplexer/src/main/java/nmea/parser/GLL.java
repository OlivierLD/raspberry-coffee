package nmea.parser;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GLL extends NMEAComposite implements Serializable {

	private Date gllTime = null;
	private GeoPos gllPos = null;

	private final static SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss.SS");

	public GLL gllDate(Date date) {
		this.gllTime = date;
		return this;
	}

	public GLL gllPos(GeoPos pos) {
		this.gllPos = pos;
		return this;
	}

	public Date getGllTime() {
		return gllTime;
	}

	public GeoPos getGllPos() {
		return gllPos;
	}

	// @Override
	public static String getCsvHeader(String separator) {
		return String.format("latitude%slongitude%sdate-time%sfmt-date-time", SEP, SEP, SEP).replace(SEP, separator);
	}

	@Override
	public String getCsvData(String separator) {
		return String.format("%s%s%s%s%s%s%s",
				(gllPos != null ? String.valueOf(gllPos.lat) : ""), separator,
				(gllPos != null ? String.valueOf(gllPos.lng) : ""), separator,
				(gllTime != null ? String.valueOf(gllTime.getTime()) : ""), separator,
				(gllTime != null ? SDF.format(gllTime) : ""));
	}

	@Override
	public String toString() {
		return String.format("%s, %s", this.gllPos.toString(), SDF.format(this.gllTime));
	}
}
