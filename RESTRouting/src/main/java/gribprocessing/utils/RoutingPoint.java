package gribprocessing.utils;

import calc.GeoPoint;

import java.awt.Point;
import java.util.Date;

public class RoutingPoint extends Point {
	private RoutingPoint parent = null;

	int xOffset = 0;
	int yOffset = 0;

	double xFactor = 1D;
	double yFactor = 1D;

	private GeoPoint position = null;
	private Date date = null;
	private double bsp = 0;
	private int hdg = 0;
	private double tws = 0;
	private int twd = 0;
	private int twa = 0;

	private boolean gribTooOld = false;

	public RoutingPoint(int x, int y) {
		super(x, y);
	}

	public RoutingPoint(Point p) {
		super(p);
	}

	public Point getPoint() {
		return new Point((int) ((x - xOffset) * xFactor), (int) ((y - yOffset) * yFactor));
	}

	public RoutingPoint getAncestor() {
		return parent;
	}

	public void setAncestor(RoutingPoint ip) {
		parent = ip;
	}

	public void setPosition(GeoPoint position) {
		this.position = position;
	}

	public GeoPoint getPosition() {
		return position;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public void setBsp(double bsp) {
		this.bsp = bsp;
	}

	public double getBsp() {
		return bsp;
	}

	public void setHdg(int hdg) {
		this.hdg = hdg;
	}

	public int getHdg() {
		return hdg;
	}

	public void setTws(double tws) {
		this.tws = tws;
	}

	public double getTws() {
		return tws;
	}

	public void setTwd(int twd) {
		this.twd = twd;
	}

	public int getTwd() {
		return twd;
	}

	public void setTwa(int twa) {
		this.twa = twa;
	}

	public int getTwa() {
		return twa;
	}

	public void setGribTooOld(boolean gribTooOld) {
		this.gribTooOld = gribTooOld;
	}

	public boolean isGribTooOld() {
		return gribTooOld;
	}
}
