package oliv.misc;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Given four points, what should be the shortest way
 * to go through each of them?
 */
public class MinimalTrack {

	private static boolean verbose = "true".equals(System.getProperty("verbose"));

	public static class Point {
		String name;
		int x;
		int y;

		public Point name(String name) {
			this.name = name;
			return this;
		}
		public Point x(int x) {
			this.x = x;
			return this;
		}
		public Point y(int y) {
			this.y = y;
			return this;
		}

		public double dist(Point pt) {
			return Math.sqrt(Math.pow(pt.x - this.x, 2) + Math.pow(pt.y - this.y, 2));
		}
	}

	public static class Route {
		String name;
		double len;
		public Route name(String name) {
			this.name = name;
			return this;
		}
		public Route len(double len) {
			this.len = len;
			return this;
		}
		public double getLen() {
			return this.len;
		}
	}

	private static Point[] generatePoints(String propFile) throws Exception {
		List<Point> list = new ArrayList<>();
		Properties props = new Properties();
		props.load(new FileReader(propFile));

		int pt = 1;
		while (true) {
			String name = props.getProperty(String.format("p%d.name", pt));
			if (name != null) {
				int x = Integer.parseInt(props.getProperty(String.format("p%d.x", pt)));
				int y = Integer.parseInt(props.getProperty(String.format("p%d.y", pt)));
				Point p = new Point().name(name).x(x).y(y);
				list.add(p);
				pt++;
			} else {
				break;
			}
		}
		return list.toArray(new Point[list.size()]);
	}

	public static void main(String... args) throws Exception {

		Point[] pointArray;

		String propertyFile = System.getProperty("props");
		if (propertyFile == null) {
			// Hard coded default point list
			Point A = new Point().x(0).y(0).name("A");
			Point B = new Point().x(300).y(0).name("B");
			Point C = new Point().x(300).y(500).name("C");
			Point D = new Point().x(0).y(500).name("D");

			pointArray = new Point[]{A, B, C, D};
		} else {
			System.out.println(String.format("Running from %s", System.getProperty("user.dir")));
			pointArray = generatePoints(propertyFile);
		}

		List<Point> pointList = Arrays.asList(pointArray);
		List<Route> routeChoices = new ArrayList<>();

		for (int startFrom=0; startFrom < pointArray.length; startFrom++) {

			Point startPoint = pointArray[startFrom];
			if (verbose) {
				System.out.println(String.format("Starting from %s", startPoint.name));
			}

			List<Point> path = new ArrayList<>();
			path.add(startPoint);

			List<Point> toEvaluate = pointList
					.stream()
					.filter(pt -> !pt.equals(startPoint)) // All points except the one you start from
					.collect(Collectors.toList());

			Point from = startPoint;
			for (int i=0; i<toEvaluate.size(); i++) {
				int closestPointIndex = -1;
				double smallestDist = Double.MAX_VALUE;
				for (int prog=0; prog<toEvaluate.size(); prog++) {
					if (!from.equals(toEvaluate.get(prog))) {
						double dist = from.dist(toEvaluate.get(prog));
						if (dist < smallestDist && !path.contains(toEvaluate.get(prog))) {
							closestPointIndex = prog;
							smallestDist = dist;
						}
					}
				}
				if (closestPointIndex != -1) {
					if (verbose) {
						System.out.println(String.format("\tClosest from %s is %s (%f)", from.name, toEvaluate.get(closestPointIndex).name, smallestDist));
					}
					path.add(toEvaluate.get(closestPointIndex));
					from = toEvaluate.get(closestPointIndex);
				}
			}
			String pathStr = path // This will be the route's name
					.stream()
					.map(pt -> pt.name)
					.collect(Collectors.joining(", "));
			Point lastPoint = null;
			double routeLen = 0;
			for (Point pt : path) {
				if (lastPoint != null) {
					routeLen += lastPoint.dist(pt);
				}
				lastPoint = pt;
			}
			if (verbose) {
				System.out.println(String.format("Result: For path %s, length is %f ", pathStr, routeLen));
			}
			routeChoices.add(new Route()
					.name(pathStr)
					.len(routeLen));
		}
		// Find shortest route
		Route bestRoute = routeChoices
				.stream()
				.min(Comparator.comparing(Route::getLen))
				.get();
		System.out.println(String.format(">> Shortest route is %s, %f", bestRoute.name, bestRoute.len));
	}
}
