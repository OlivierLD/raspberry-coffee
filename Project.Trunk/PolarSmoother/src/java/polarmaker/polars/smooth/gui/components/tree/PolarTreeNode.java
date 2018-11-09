package polarmaker.polars.smooth.gui.components.tree;

import polarmaker.smooth.PolarsResourceBundle;

import java.text.DecimalFormat;

public class PolarTreeNode
		extends DefaultDataTreeNode {
	public final static int ROOT_TYPE = 0;
	public final static int TWS_TYPE = 1;
	public final static int TWA_TYPE = 2;
	public final static int SECTION_TYPE = 3;

	private int type;
	private String model = "";
	private double tws = 0.0;
	private double upwindSpeed = 0.0;
	private double upwindTwa = 0.0;
	private double upwindVmg = 0.0;
	private double downwindSpeed = 0.0;
	private double downwindTwa = 0.0;
	private double downwindVmg = 0.0;
	private int twa = 0;
	private double bsp = 0.0;

	private int polarDegree = 0;
	private int coeffDegree = 0;
	private int fromTwa = 0;
	private int toTwa = 0;

	DecimalFormat df = new DecimalFormat("##0.0");

	// Root
	public PolarTreeNode(String name) {
		super(name);
		this.model = name;
		this.type = ROOT_TYPE;
	}

	// Section, or anything.
	public PolarTreeNode(String name, int type) {
		super(name);
		this.model = name;
		this.type = type;
	}

	// Section
	public PolarTreeNode(String name, int polDeg, int coefDeg, int fromTwa, int toTwa) {
		super(name);
		this.model = name;
		this.type = SECTION_TYPE;
		this.setPolarDegree(polDeg);
		this.setCoeffDegree(coefDeg);
		this.setFromTwa(fromTwa);
		this.setToTwa(toTwa);
	}

	// tws
	public PolarTreeNode(double tws,
	                     double upwindSpeed,
	                     double upwindTwa,
	                     double upwindVmg,
	                     double downwindSpeed,
	                     double downwindTwa,
	                     double downwindVmg) {
		super("TWS");
		this.tws = tws;
		this.upwindSpeed = upwindSpeed;
		this.upwindTwa = upwindTwa;
		this.upwindVmg = upwindVmg;
		this.downwindSpeed = downwindSpeed;
		this.downwindTwa = downwindTwa;
		this.downwindVmg = downwindVmg;
		this.type = TWS_TYPE;
	}

	// twa
	public PolarTreeNode(int twa, double bsp) {
		super("TWA");
		this.twa = twa;
		this.bsp = bsp;
		this.type = TWA_TYPE;
	}

	public int getType() {
		return this.type;
	}

	/**
	 * Generates what's displayed in the tree.
	 *
	 * @return What's displayed in the tree...
	 */
	public String toString() {
		String ret = "";
		switch (this.type) {
			case PolarTreeNode.ROOT_TYPE:
				ret = PolarsResourceBundle.getPolarsResourceBundle().getString("polars-for") + model;
				break;
			case PolarTreeNode.SECTION_TYPE:
				ret = "section:" + model;
				break;
			case PolarTreeNode.TWS_TYPE:
				ret = "tws:";
				ret += Double.toString(tws);
				break;
			case PolarTreeNode.TWA_TYPE:
				ret = "twa:" + Integer.toString(twa) + ", bsp:" + df.format(bsp);
				break;
			default:
				ret = "Unknown node type";
				break;
		}
		return ret;
	}

	public double getBsp() {
		return bsp;
	}

	public double getDownwindSpeed() {
		return downwindSpeed;
	}

	public double getDownwindTwa() {
		return downwindTwa;
	}

	public double getDownwindVmg() {
		return downwindVmg;
	}

	public String getModel() {
		return model;
	}

	public int getTwa() {
		return twa;
	}

	public double getTws() {
		return tws;
	}

	public double getUpwindSpeed() {
		return upwindSpeed;
	}

	public double getUpwindTwa() {
		return upwindTwa;
	}

	public double getUpwindVmg() {
		return upwindVmg;
	}

	public void setBsp(double bsp) {
		this.bsp = bsp;
	}

	public void setDownwindSpeed(double downwindSpeed) {
		this.downwindSpeed = downwindSpeed;
	}

	public void setDownwindTwa(double downwindTwa) {
		this.downwindTwa = downwindTwa;
	}

	public void setDownwindVmg(double downwindVmg) {
		this.downwindVmg = downwindVmg;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void setTwa(int twa) {
		this.twa = twa;
	}

	public void setTws(double tws) {
		this.tws = tws;
	}

	public void setUpwindSpeed(double upwindSpeed) {
		this.upwindSpeed = upwindSpeed;
	}

	public void setUpwindTwa(double upwindTwa) {
		this.upwindTwa = upwindTwa;
	}

	public void setUpwindVmg(double upwindVmg) {
		this.upwindVmg = upwindVmg;
	}

	public void setPolarDegree(int polarDegree) {
		this.polarDegree = polarDegree;
	}

	public int getPolarDegree() {
		return polarDegree;
	}

	public void setCoeffDegree(int coeffDegree) {
		this.coeffDegree = coeffDegree;
	}

	public int getCoeffDegree() {
		return coeffDegree;
	}

	public void setFromTwa(int fromTwa) {
		this.fromTwa = fromTwa;
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
