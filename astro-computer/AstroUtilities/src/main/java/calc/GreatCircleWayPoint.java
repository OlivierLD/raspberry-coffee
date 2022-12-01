package calc;

public class GreatCircleWayPoint {
	private final GreatCirclePoint p;
	private final Double z;

	@Override
	public String toString() {
		return "GreatCircleWayPoint{" +
				"p=" + p +
				", z=" + z +
				'}';
	}

	public GreatCircleWayPoint(GreatCirclePoint p, Double z) {
		this.p = p;
		this.z = z;
	}

	public GreatCirclePoint getPoint() {
		return p;
	}

	public Double getZ() {
		return z;
	}
}
