package oliv.streams;

import java.util.ArrayList;
import java.util.List;

public class DataForNeurons {

	public static class Point {
		private double x;
		private double y;

		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double getX() {
			return this.x;
		}
		public double getY() {
			return this.y;
		}
	}

	public static void main(String... args) {
		// Create random point lists
		List<Point> orangeList = new ArrayList<>();
		List<Point> blueList = new ArrayList<>();


		for (int i = 0; i < 500; i++) {
			double x = (-6d * Math.random());
			double y = (-6d * Math.random());
			orangeList.add(new Point(x, y));
		}

		for (int i = 0; i < 500; i++) {
			double x = (-6d * Math.random());
			double y = (-6d * Math.random());
			blueList.add(new Point(x, y));
		}

		double[] xData = orangeList.stream()
				.mapToDouble(Point::getX)
				.toArray();
		double[] yData = orangeList.stream()
				.mapToDouble(Point::getY)
				.toArray();
	}
}
