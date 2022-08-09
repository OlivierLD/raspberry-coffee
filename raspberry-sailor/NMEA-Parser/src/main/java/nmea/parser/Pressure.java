package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public class Pressure implements Serializable {
	private double pressure = 0d; // In hPa
	private static final String H_PA = " hPa";
	private static final String MM_HG = " mmHg";
	private static final String IN_HG = "\"Hg";
	private static final String PSI = " psi";
	private static final String ATM = " Atm";
	private static final DecimalFormat FMT = new DecimalFormat("###0.0");

	public final static double HPA_TO_mmHG = 1.3332239;
	public final static double HPA_TO_INHG = 33.8638816;
	public final static double HPA_TO_PSI = 68.9475729;
	public final static double HPA_TO_ATM = 1_013.25;

	public Pressure() {
	}

	public Pressure(double press) {
		this.pressure = press;
	}

	public double getValue() {
		return this.pressure;
	}

	public String toString() {
		double d = this.pressure;
		return FMT.format(d) + H_PA + ", " +
						FMT.format(d / HPA_TO_mmHG) + MM_HG + ", " +
						FMT.format(d / HPA_TO_INHG) + IN_HG + ", " +
						FMT.format(d / HPA_TO_PSI) + PSI + ", " +
						FMT.format(d / HPA_TO_ATM) + ATM;
	}
}
