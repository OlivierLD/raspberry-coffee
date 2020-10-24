package tideengine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TideStation implements Serializable {
	@SuppressWarnings("compatibility:388041214676602538")
	private final static long serialVersionUID = 1L;

	public final static String METERS = "meters";
	public final static String FEET = "feet";
	public final static String KNOTS = "knots";
	public final static String SQUARE_KNOTS = "knots^2";

	private String fullName = "";
	private List<String> nameParts = new ArrayList<>();
	private double latitude = 0D;
	private double longitude = 0D;
	private double baseHeight = 0D;
	private String unit = "";
	private String timeZone = "";
	private String timeOffset = "";
	private List<Harmonic> harmonics = new ArrayList<>();

	private int harmonicsHaveBeenFixedForYear = -1;

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public List<String> getNameParts() {
		return nameParts;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setBaseHeight(double baseHeight) {
		this.baseHeight = baseHeight;
	}

	public double getBaseHeight() {
		return baseHeight;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getUnit() {
		return unit;
	}

	public List<Harmonic> getHarmonics() {
		return harmonics;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeOffset(String timeOffset) {
		this.timeOffset = timeOffset;
	}

	public String getTimeOffset() {
		return timeOffset;
	}

	public boolean isCurrentStation() {
		return unit.startsWith(KNOTS);
	}

	public boolean isTideStation() {
		return !unit.startsWith(KNOTS);
	}

	public String getDisplayUnit() {
		if (unit.equals(SQUARE_KNOTS)) {
			return KNOTS;
		} else {
			return unit;
		}
	}

	public void setHarmonicsFixedForYear(int y) {
		this.harmonicsHaveBeenFixedForYear = y;
	}

	public int yearHarmonicsFixed() {
		return harmonicsHaveBeenFixedForYear;
	}

	@Override
	public String toString() {
		return this.getFullName();
	}
}
