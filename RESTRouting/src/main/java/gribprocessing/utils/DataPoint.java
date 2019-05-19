package gribprocessing.utils;

public class DataPoint {
	public double x = 0D, y = 0D, d = 0D, s = 0D, dC = 0D, sC = 0D;
	public double prmsl = 0D,
			hgt500 = 0D,
			temp = 0D,
			whgt = 0D,
			rain = 0D;
	public float u = 0, v = 0;
	public float uC = 0, vC = 0;

	public DataPoint() {
	}

	public DataPoint(double x, double y, float u, float v, double dir, double speed) {
		this.x = x;
		this.y = y;
		this.u = u;
		this.v = v;
		this.d = dir;
		this.s = speed;
	}

	public DataPoint(double x,
	                 double y,
	                 float u,
	                 float v,
	                 double dir,
	                 double speed,
	                 double prmsl,
	                 double hgt500,
	                 double temp,
	                 double whgt,
	                 double rain,
	                 float uC,
	                 float vC,
	                 double cdr,
	                 double csp) {
		this.x = x;
		this.y = y;
		this.u = u;
		this.v = v;
		this.d = dir;
		this.s = speed;
		this.prmsl = prmsl;
		this.hgt500 = hgt500;
		this.temp = temp;
		this.whgt = whgt;
		this.rain = rain;
		this.uC = uC;
		this.vC = vC;
		this.dC = cdr;
		this.sC = csp;
	}
}
