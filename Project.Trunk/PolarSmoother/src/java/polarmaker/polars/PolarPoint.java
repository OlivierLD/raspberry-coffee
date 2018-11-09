package polarmaker.polars;

public final class PolarPoint {
	double bsp = 0.0;
	double twa = 0.0;
	double tws = 0.0;
	double awa = 0.0;
	double aws = 0.0;
	int hdm = 0;

	public PolarPoint(double bsp,
	                  double twa) {
		this(bsp, twa, 0.0, 0);
	}

	public PolarPoint(double bsp,
	                  double twa,
	                  double tws,
	                  int hdm) {
		this(bsp, -1.0, -1.0, twa, tws, hdm);
	}

	public PolarPoint(double bsp,
	                  double awa,
	                  double aws,
	                  double twa,
	                  double tws,
	                  int hdm) {
		this.bsp = bsp;
		this.awa = awa;
		this.aws = aws;
		this.twa = twa;
		this.tws = tws;
		this.hdm = hdm;
	}

	public double getBsp() {
		return this.bsp;
	}

	public double getAwa() {
		return this.awa;
	}

	public double getAws() {
		return this.aws;
	}

	public double getTwa() {
		return this.twa;
	}

	public double getTws() {
		return this.tws;
	}

	public int getHdm() {
		return this.hdm;
	}
}
