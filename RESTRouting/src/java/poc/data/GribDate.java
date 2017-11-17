package poc.data;

import java.util.Date;

public class GribDate
		extends Date {
	private Date date;
	private int height;
	private int width;
	private double stepx;
	private double stepy;
	private double top, bottom, left, right;

	public GribDate(Date d, int h, int w, double x, double y, double t,
	                double b, double l, double r) {
		super(d.getTime());
		this.date = d;
		this.height = h;
		this.width = w;
		this.stepx = x;
		this.stepy = y;
		this.left = l;
		this.right = r;
		this.top = t;
		this.bottom = b;
	}

	public void setGDate(Date date) {
		this.date = date;
	}

	public Date getGDate() {
		return date;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setStepx(double stepx) {
		this.stepx = stepx;
	}

	public double getStepx() {
		return stepx;
	}

	public void setStepy(double stepy) {
		this.stepy = stepy;
	}

	public double getStepy() {
		return stepy;
	}

	public void setTop(double top) {
		this.top = top;
	}

	public double getTop() {
		return top;
	}

	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	public double getBottom() {
		return bottom;
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public double getLeft() {
		return left;
	}

	public void setRight(double right) {
		this.right = right;
	}

	public double getRight() {
		return right;
	}
}
