package nmea.parser;

import java.io.Serializable;

public class SVData implements Serializable {
	int svID = 0,
					elevation = 0,
					azimuth = 0,
					snr = 0;

	public SVData(int id, int elev, int z, int snr) {
		this.svID = id;
		this.elevation = elev;
		this.azimuth = z;
		this.snr = snr;
	}

	public void setSvID(int svID) {
		this.svID = svID;
	}

	public int getSvID() {
		return svID;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}

	public int getElevation() {
		return elevation;
	}

	public void setAzimuth(int azimuth) {
		this.azimuth = azimuth;
	}

	public int getAzimuth() {
		return azimuth;
	}

	public void setSnr(int snr) {
		this.snr = snr;
	}

	public int getSnr() {
		return snr;
	}
}
