package polarmaker.polars.smooth.gui.components.polars;

public class CoeffForSection {
	private String sectionName = "";
	private double[][] coeff;

	public CoeffForSection(String name, double[][] daa) {
		this.sectionName = name;
		this.coeff = daa;
	}

	public double[][] getCoeff() {
		return coeff;
	}

	public String getSectionName() {
		return sectionName;
	}
}
