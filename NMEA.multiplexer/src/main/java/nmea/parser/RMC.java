package nmea.parser;

import java.io.Serializable;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

public class RMC implements Serializable {
	private GeoPos gp = null;
	private double sog = 0D;
	private double cog = 0D;

	private boolean valid = false; // False means warning.

	private Date rmcDate = null;
	private Date rmcTime = null;
	private double declination = -Double.MAX_VALUE;

	public enum RMC_TYPE {
		AUTONOMOUS,
		DIFFERENTIAL,
		ESTIMATED,
		NOT_VALID,
		SIMULATOR
	}

	private RMC_TYPE rmcType = null;

	private final static SimpleDateFormat SDF = new SimpleDateFormat("E dd-MMM-yyyy HH:mm:ss.SS");

	static {
		SDF.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	public RMC() {
	}

	public RMC setGp(GeoPos gp) {
		this.gp = gp;
		return this;
	}

	public GeoPos getGp() {
		return gp;
	}

	public RMC setSog(double sog) {
		this.sog = sog;
		return this;
	}

	public double getSog() {
		return sog;
	}

	public RMC setCog(double cog) {
		this.cog = cog;
		return this;
	}

	public double getCog() {
		return cog;
	}

	public RMC setValid(boolean b) {
		this.valid = b;
		return this;
	}

	public boolean isValid() {
		return this.valid;
	}

	public String toString() {
		String str = "";
		str = (gp != null ? gp.toString() : "[no pos]") + (!valid ? "(Warning)" : "") + ", " + "SOG:" + sog + ", COG:" + cog;
		if (rmcDate != null) {
			str += (" " + SDF.format(rmcDate) + " ");
		}
		if (declination != -Double.MAX_VALUE) {
			str += ("D:" + Double.toString(declination));
		}
		if (rmcType != null) {
			if (!str.endsWith(" ")) {
				str += " ";
			}
			str += ("[ " + rmcType.toString() + " ]");
		}
		return str;
	}

	public RMC setRmcDate(Date rmcDate) {
		this.rmcDate = rmcDate;
		return this;
	}

	public Date getRmcDate() {
		return rmcDate;
	}

	public RMC setDeclination(double declination) {
		this.declination = declination;
		return this;
	}

	public double getDeclination() {
		return declination;
	}

	public RMC setRmcTime(Date rmcTime) {
		this.rmcTime = rmcTime;
		return this;
	}

	public Date getRmcTime() {
		return rmcTime;
	}

	public RMC setRmcType(RMC_TYPE type) {
		this.rmcType = type;
		return this;
	}

	public RMC_TYPE getRmcType() {
		return this.rmcType;
	}
}
