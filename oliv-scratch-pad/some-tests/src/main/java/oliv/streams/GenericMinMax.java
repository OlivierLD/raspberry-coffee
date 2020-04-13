package oliv.streams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GenericMinMax {

	public static class Point {
		double x;
		double y;

		public Point() {}
		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public Point x(double x) {
			this.x = x;
			return this;
		}
		public Point y(double y) {
			this.y = y;
			return this;
		}

		public double getX() {
			return this.x;
		}
		public double getY() {
			return this.y;
		}
	}

	public static void main(String... args) {
		List<Point> pointList = new ArrayList<>();
		pointList.add(new Point(1, 2));
		pointList.add(new Point(-10, 3));
		pointList.add(new Point(2, 2));
		pointList.add(new Point(3, 2));
		pointList.add(new Point(-1, 2));
		pointList.add(new Point(5, 2));

		double minX = pointList
				.stream()
				.min(Comparator.comparing(Point::getX)) // Compare on that method's output. Could be anything.
				.get()
				.getX();

		System.out.println(String.format("MinX: %f", minX));
	}
}
