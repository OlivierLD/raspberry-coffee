package polarmaker.polars.smooth.gui.components.polars;

public class CoeffForPolars {
	private double[][] coeffDeg;
	private int polarDegree;
	private int fromTwa;
	private int toTwa;

	public CoeffForPolars(double[][] daa, int pDeg, int from, int to) {
		this.coeffDeg = daa;
		this.polarDegree = pDeg;
		this.fromTwa = from;
		this.toTwa = to;
	}

	public double[][] getCoeffDeg() {
		return coeffDeg;
	}

	public int getPolarDegree() {
		return polarDegree;
	}

	public int getFromTwa() {
		return fromTwa;
	}

	public void setToTwa(int toTwa) {
		this.toTwa = toTwa;
	}

	public int getToTwa() {
		return toTwa;
	}
}
