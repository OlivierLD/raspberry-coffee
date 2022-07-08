package nmea.parser;

import java.io.Serializable;

public class VHW extends NMEAComposite implements Serializable {

	private double bsp = 0d;
	private double hdm = 0d;
	private double hdg = 0d;

	public VHW bsp(double bsp) {
		this.bsp = bsp;
		return this;
	}

	public VHW hdm(double hdm) {
		this.hdm = hdm;
		return this;
	}

	public VHW hdg(double hdg) {
		this.hdg = hdg;
		return this;
	}

	public double getBsp() {
		return bsp;
	}

	public double getHdm() {
		return hdm;
	}

	public double getHdg() {
		return hdg;
	}

	// @Override
	public static String getCsvHeader(String separator) {
		return String.format("bsp%shdg%shdm", SEP, SEP).replace(SEP, separator);
	}

	@Override
	public String getCsvData(String separator) {
		return String.format("%f%s%f%s%f",
				bsp, separator,
				hdm, separator,
				hdg);
	}

	@Override
	public String toString() {
		return String.format("BSP: %.02f, HDM: %d, HDG: %d", bsp, (int)Math.round(hdm), (int)Math.round(hdg));
	}
}
