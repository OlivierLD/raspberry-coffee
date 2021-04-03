package oliv.streams;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

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

	/**
	 * Standard deviation:
	 * stdDev = Math.sqrt((sum((Xi - meanX) ^ 2) / card)
	 *
	 * Good doc at https://www.mathsisfun.com/data/standard-deviation.html
	 */
	private static double stdDev(List<Double> list) {
		double mean = list.stream().mapToDouble(x -> x).average().getAsDouble();
		double variance = list.stream().mapToDouble(x -> Math.pow(x - mean, 2)).sum() / list.size();
		double stdDev = Math.sqrt(variance);
		return stdDev;
	}

	// A test
	private static void testStdDev() {
		List<Double> list = Arrays.asList(600.0, 470.0, 170.0, 430.0, 300.0);
		System.out.printf("StdDev: %f %n", stdDev(list));
	}

	private final static int WIDTH = 100;
	private final static int HEIGHT = 100;
	private final static int CARDINALITY = 10_000;

	public static void main(String... args) {

//		testStdDev();
//		System.exit(0);

		List<Point> pointList = new ArrayList<>();
//		pointList.add(new Point(1, 2));
//		pointList.add(new Point(-10, 3));
//		pointList.add(new Point(2, 2));
//		pointList.add(new Point(3, 2));
//		pointList.add(new Point(-1, 2));
//		pointList.add(new Point(5, 2));

		while (pointList.size() < CARDINALITY) {
			pointList.add(new Point(
					(WIDTH * Math.random()) - (WIDTH / 2),
					(HEIGHT * Math.random()) - (HEIGHT / 2)));
		}

		double minX = pointList.stream()
				.min(Comparator.comparing(Point::getX)) // Compare on that method's output (getX). Could be anything.
				.get()
				.getX();
		double maxX = pointList.stream()
				.max(Comparator.comparing(Point::getX))
				.get()
				.getX();
		double minY = pointList.stream()
				.min(Comparator.comparing(Point::getY))
				.get()
				.getY();
		double maxY = pointList.stream()
				.max(Comparator.comparing(Point::getY))
				.get()
				.getY();

		double averageX = pointList.stream()
				.mapToDouble(Point::getX)
				.average()
				.getAsDouble();
		double averageY = pointList.stream()
				.mapToDouble(Point::getY)
				.average()
				.getAsDouble();

		System.out.format("On %s points:%n", NumberFormat.getInstance().format(pointList.size()));
		System.out.format("MinX: %f, MaxX: %f, MinY: %f, MaxY: %f%n", minX, maxX, minY, maxY);
		System.out.format("AvgX: %f, AvgY: %f%n", averageX, averageY);

		List<Double> xList = pointList.stream().mapToDouble(Point::getX).boxed().collect(Collectors.toList());
		List<Double> yList = pointList.stream().mapToDouble(Point::getY).boxed().collect(Collectors.toList());

		double stdDevX = stdDev(xList);
		double stdDevY = stdDev(yList);
		System.out.format("Std Dev X: %f, Std Dev Y: %f%n", stdDevX, stdDevY);

		// Sort the positive Xs, print the 100 first ones.
		pointList.stream()
				.filter(pt -> pt.getX() > 0)
				.mapToDouble(Point::getX)
				.sorted()
				.limit(100)
				.forEach(System.out::println);
	}
}
