package nmea.parser;

import java.io.Serializable;

public class RMB extends NMEAComposite implements Serializable {
	private GeoPos dest;  // Destination Waypoint position
	private String owpid; // Origin WP ID
	private String dwpid; // Destination WP ID
	private double xte;   // Cross Track Error
	private String dts;   // Direction to steer
	private double rtd;   // Range to Destination
	private double btd;   // Bearing to destination (true)
	private double dcv;   // Destination closing velocity
	private String as;    // Arrival Status (A: entered or perpendicular passed)

	public RMB() {
	}

	public void setDest(GeoPos dest) {
		this.dest = dest;
	}

	public GeoPos getDest() {
		return dest;
	}

	public void setOwpid(String owpid) {
		this.owpid = owpid;
	}

	public String getOwpid() {
		return owpid;
	}

	public void setDwpid(String dwpid) {
		this.dwpid = dwpid;
	}

	public String getDwpid() {
		return dwpid;
	}

	public void setXte(double xte) {
		this.xte = xte;
	}

	public double getXte() {
		return xte;
	}

	public void setDts(String dts) {
		this.dts = dts;
	}

	public String getDts() {
		return dts;
	}

	public void setRtd(double rtd) {
		this.rtd = rtd;
	}

	public double getRtd() {
		return rtd;
	}

	public void setBtd(double btd) {
		this.btd = btd;
	}

	public double getBtd() {
		return btd;
	}

	public void setDcv(double dcv) {
		this.dcv = dcv;
	}

	public double getDcv() {
		return dcv;
	}

	public void setAs(String as) {
		this.as = as;
	}

	public String getAs() {
		return as;
	}

	// @Override
	public static String getCsvHeader(String separator) {
		return String.format("dest-lat%sdest-lng%sorig-wp%sdest-wp%sxte%sdts%srtd%sbtd%sdvc%sas", SEP, SEP, SEP, SEP, SEP, SEP, SEP, SEP, SEP).replace(SEP, separator);
	}

	@Override
	public String getCsvData(String separator) {
		return String.format("%s%s%s%s%s%s%s%s%f%s%s%s%f%s%f%s%f%s%s",
				(this.dest != null ? String.valueOf(this.dest.lat) : ""), separator,
				(this.dest != null ? String.valueOf(this.dest.lng) : ""), separator,
				owpid, separator,
				dwpid, separator,
				xte, separator,
				dts, separator,
				rtd, separator,
				btd, separator,
				dcv, separator,
				as);
	}
}
