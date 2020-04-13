package matrix;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class SystemUtil {

	public static double[] solveSystem(double[] m,
	                                   double[] c) {
		SquareMatrix sma = new SquareMatrix(c.length);

		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c.length; j++) {
				sma.setElementAt(i, j, m[(c.length * i) + j]);
			}
		}
		return solveSystem(sma, c);
	}

	/**
	 * Solves a system, n equations, n unknowns.
	 * <p>
	 * the values we look for are x, y, z.
	 * <pre>
	 * ax + by + cz = X
	 * Ax + By + Cz = Y
	 * Px + Qy + Rz = Z
	 * </pre>
	 * @param m Coeffs matrix, n x n (left) from the system above
	 * <pre>
	 * | a b c |
	 * | A B C |
	 * | P Q R |
	 * </pre>
	 * @param c Constants array, n (right) <code>[X, Y, Z]</code> from the system above
	 * @return the unknown array, n. <code>[x, y, z]</code> from the system above
	 */
	public static double[] solveSystem(SquareMatrix m,
	                                   double[] c) {
		double[] result;
		result = new double[m.getDimension()];

		SquareMatrix inv = MatrixUtil.invert(m);

		// Print inverted Matrix
		if ("true".equals(System.getProperty("debug", "false"))) {
			System.out.println("Inverted:");
			MatrixUtil.printMatrix(inv);
		}

		// Lines * Column
		for (int row = 0; row < m.getDimension(); row++) {
			result[row] = 0.0;
			for (int col = 0; col < m.getDimension(); col++) {
				result[row] += (inv.getElementAt(row, col) * c[col]);
			}
		}
		return result;
	}

	public static void printSystem(SquareMatrix squareMatrix, double[] constants) {
		String unknowns = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		// Nice IntStream s !
		IntStream.range(0, squareMatrix.getDimension()).forEach(row -> {
			final StringBuffer sb = new StringBuffer();
			IntStream.range(0, squareMatrix.getDimension()).forEach(col -> {
				sb.append(String.format("%s(%f x %c)", (!sb.toString().trim().isEmpty() ? " + " : ""), squareMatrix.getElementAt(row, col), unknowns.charAt(col)));
			});
			sb.append(String.format(" = %f", constants[row]));
			System.out.println(sb.toString());
		});
	}

	public static String funcToString(String prefix, double curveCoeff[]) {
		String str = prefix + " = ";
		for (int degree=0; degree<curveCoeff.length; degree++) {
			int exp = curveCoeff.length - 1 - degree;
			str += (String.format("%+f %s ", curveCoeff[degree], (exp == 0 ? "" : (exp == 1 ? "* x" : (String.format("* x^%d", exp))))));
		}
		return str;
	}

	/**
	 * Distance to each point of the curve y=f(x) is [(x - ptX)^2 + (f(x) - ptY)^2] ^ (1/2)
	 * To get rid of the sqrt:
	 * dist^2 = (x - ptX)^2 + (f(x) - ptY)^2
	 * For the minimal distance, we are looking for roots of the derivative of the above.
	 *
	 * [f o g (x)]' = f(g(x))' = f'(g(x)) x g'(x)
	 *
	 * @param curve highest degree first
	 * @param pt
	 * @return
	 */
	public static double minDistanceToCurve(double curve[], PolynomUtil.Point pt) {
		double dist = Double.MAX_VALUE;

		// distance pt = curve = distance between (x, f(x)) and (0, 3)
		// = (deltaX^2 + deltaY^2)^(1/2)
		//<=> distance^2 = (deltaX^2 + deltaY^2)
		// = (x - ptX)^2 + (f(x) - ptY)^2
		// Derivative: [2*(x-ptX)] + [2*(f(x) - ptY)*(f'(x))]
		//              |-------|     |--+--------+---+--+-|
		//              |             |  |--------|   |--|
		//              |             |  |            |
		//              |             |  |            2-2
		//              |             |  2-1
		//              |             Part 2
		//              Part 1
		// Minimal distance is the smallest of the roots of the derivative above.

		// 2*(x-ptX)
		double[] part1 = PolynomUtil.multiply(new double[] { 1, -pt.x }, new double[] { 2 });

		// (f(x) - ptY)
		double[] part21 = PolynomUtil.add(curve, new double[] { -pt.y });
		// f'(x)
		double[] part22 = PolynomUtil.derivative(curve);
		// 2 * (f(x) - ptY) * (f'(x))
		double[] part2 = PolynomUtil.multiply(PolynomUtil.multiply(part21, part22), new double[] { 2 });
		// [2 * (x-ptX)] + [2 * (f(x) - ptY) * (f'(x))]
		double[] full = PolynomUtil.reduce(PolynomUtil.add(part1, part2));
		if ("true".equals(System.getProperty("system.verbose"))) {
			System.out.println(String.format(">> minDistanceToCurve, resolving %s", PolynomUtil.display(full)));
		}
		List<Double> polynomRoots = PolynomUtil.getPolynomRoots(full);
		if (polynomRoots.size() == 0) {
			throw new RuntimeException("no root");
		} else {
			for (double r : polynomRoots) {
				dist = Math.min(dist, PolynomUtil.dist(curve, r, pt));
			}
		}
		return dist;
	}

	public static double[] smooth(List<PolynomUtil.Point> data, int requiredDegree) {
		int dimension = requiredDegree + 1;
		double[] sumXArray = new double[(requiredDegree * 2) + 1]; // Will fill the matrix
		double[] sumY      = new double[requiredDegree + 1];
		// Init
		for (int i=0; i<((requiredDegree * 2) + 1); i++) {
			sumXArray[i] = 0.0;
		}
		for (int i=0; i<(requiredDegree + 1); i++) {
			sumY[i] = 0.0;
		}

		data.stream().forEach(point -> {
			for (int i=0; i<((requiredDegree * 2) + 1); i++) {
				sumXArray[i] += Math.pow(point.x, i);
			}
			for (int i=0; i<(requiredDegree + 1); i++) {
				sumY[i] += (point.y * Math.pow(point.x, i));
			}
		});

		SquareMatrix squareMatrix = new SquareMatrix(dimension);
		for (int row=0; row<dimension; row++) {
			for (int col=0; col<dimension; col++) {
				int powerRnk = (requiredDegree - row) + (requiredDegree - col);
//			System.out.println("[" + row + "," + col + ":" + (powerRnk) + "] = " + sumXArray[powerRnk]);
				squareMatrix.setElementAt(row, col, sumXArray[powerRnk]);
			}
		}
		double[] constants = new double[dimension];
		for (int i=0; i<dimension; i++) {
			constants[i] = sumY[requiredDegree - i];
//		System.out.println("[" + (requiredDegree - i) + "] = " + constants[i]);
		}

		double[] result = SystemUtil.solveSystem(squareMatrix, constants);
		return result;
	}

	/**
	 * An example
	 * @param args unused.
	 */
	public static void main(String... args) {
		SquareMatrix squareMatrix = new SquareMatrix(3);
		/*
		  Resolution of:
		    12x    +  13y +    14z = 234
		    1.345x - 654y + 0.001z = 98.87
		    23.09x + 5.3y - 12.34z = 9.876
		 */
		squareMatrix.setElementAt(0, 0, 12);
		squareMatrix.setElementAt(0, 1, 13);
		squareMatrix.setElementAt(0, 2, 14);

		squareMatrix.setElementAt(1, 0, 1.345);
		squareMatrix.setElementAt(1, 1, -654);
		squareMatrix.setElementAt(1, 2, 0.001);

		squareMatrix.setElementAt(2, 0, 23.09);
		squareMatrix.setElementAt(2, 1, 5.3);
		squareMatrix.setElementAt(2, 2, -12.34);

		double[] constants = new double[]{234, 98.87, 9.876};

		System.out.println(String.format("Matrix Determinant: %f", MatrixUtil.determinant(squareMatrix)));

		System.out.println("Solving:");
		printSystem(squareMatrix, constants);

		long before = System.nanoTime();
		double[] result = solveSystem(squareMatrix, constants);
		long after = System.nanoTime();
		System.out.println(String.format("\nDone is %s nano sec.", DecimalFormat.getInstance().format(after - before)));

		System.out.println(String.format("A = %f", result[0]));
		System.out.println(String.format("B = %f", result[1]));
		System.out.println(String.format("C = %f", result[2]));
		System.out.println();
		// Proof:
		double X = (squareMatrix.getElementAt(0, 0) * result[0]) + (squareMatrix.getElementAt(0, 1) * result[1]) + (squareMatrix.getElementAt(0, 2) * result[2]);
		System.out.println(String.format("X: %f", X));
		double Y = (squareMatrix.getElementAt(1, 0) * result[0]) + (squareMatrix.getElementAt(1, 1) * result[1]) + (squareMatrix.getElementAt(1, 2) * result[2]);
		System.out.println(String.format("Y: %f", Y));
		double Z = (squareMatrix.getElementAt(2, 0) * result[0]) + (squareMatrix.getElementAt(2, 1) * result[1]) + (squareMatrix.getElementAt(2, 2) * result[2]);
		System.out.println(String.format("Z: %f", Z));
		System.out.println("--- With one-line SquareMatrix constructor---");
		// Using another SquareMatrix constructor
		squareMatrix = new SquareMatrix(3, 12, 13, 14, 1.345, -654, 0.001, 23.09, 5.3, -12.34);
		System.out.println("Solving:");
		printSystem(squareMatrix, constants);

		before = System.nanoTime();
		result = solveSystem(squareMatrix, constants);
		after = System.nanoTime();
		System.out.println(String.format("\nDone in %s \u212bs (nano-sec).", DecimalFormat.getInstance().format(after - before)));

		System.out.println(String.format("A = %f", result[0]));
		System.out.println(String.format("B = %f", result[1]));
		System.out.println(String.format("C = %f", result[2]));

		// Test derivative
		double[] der = PolynomUtil.derivative(new double[] { 3d, 2d, 1d, 6d});
		Arrays.stream(der).forEach(c -> System.out.print(String.format("%f ", c)));
		System.out.println();

		System.out.println();
		// Function y = f(x)
		double[] coeff = new double[] {-6, 4, 3};
		System.out.println(funcToString("y", coeff));
		double x = 3.4;
		System.out.println(String.format("for %s, x=%f, f(x)=%f", funcToString("f(x)", coeff).trim(), x, PolynomUtil.f(coeff, x)));
		List<Double> roots = PolynomUtil.getPolynomRoots(PolynomUtil.reduce(coeff));
		if (roots.size() == 0) {
			System.out.println("no root");
		} else {
			System.out.println("roots:");
			for (double r : roots) {
				System.out.println(String.format("\t%+f, f(x) = %f", r, PolynomUtil.f(coeff, r)));
			}
		}

		// Minimal distance between point and curve
		System.out.println("Minimal distance:");
		double[] curve = new double[] { -1, 0, 6, 10 };
		PolynomUtil.Point pt = new PolynomUtil.Point().x(0).y(3);
		// distance pt - curve = distance between (x, f(x)) and (0, 3)
		// = (deltaX^2 + deltaY^2)^(1/2)
		//<=> distance^2 = (deltaX^2 + deltaY^2)
		// = (x - ptX)^2 + (f(x) - ptY)^2
		// Derivative: [2*(x-ptX)] + [2*(f(x) - ptY)*(f'(x))]
		//              |             |  |            |
		//              |             |  |            Part 2-2
		//              |             |  Part 2-1
		//              |             Part 2
		//              Part 1
		// Minimal distance is the smallest of the roots of the derivative above.
		// Needed: polynomial addition, multiplication

		double[] part1 = PolynomUtil.multiply(new double[] { 1, -pt.x }, new double[] { 2 });

		double[] part21 = PolynomUtil.add(curve, new double[] { -pt.y });
		double[] part22 = PolynomUtil.derivative(curve);
		double[] part2  = PolynomUtil.multiply(PolynomUtil.multiply(part21, part22), new double[] { 2 });
		double[] full   = PolynomUtil.add(part1, part2);

		System.out.println("For curve: " + PolynomUtil.display(curve));
		System.out.println("Resolving: " + PolynomUtil.display(full));
		List<Double> polynomRoots = PolynomUtil.getPolynomRoots(PolynomUtil.reduce(full));
		if (polynomRoots.size() == 0) {
			System.out.println("no root");
		} else {
			System.out.println("roots:");
			for (double r : polynomRoots) {
				System.out.println(String.format("\t%+f, f(x) = %f, dist=%f", r, PolynomUtil.f(curve, r), PolynomUtil.dist(curve, r, pt)));
			}
		}
		// Min dist from one pt to curve
		System.out.println();
		System.setProperty("system.verbose", "true");
		double minDist = minDistanceToCurve(curve, pt);
		System.out.println(String.format("Minimal distance from %s to curve %s is %f", formatPoint(pt), PolynomUtil.display(curve), minDist));
	}

	private static String formatPoint(PolynomUtil.Point pt) {
		String formatted = "";
		if (pt.x == (long)pt.x) {
			formatted += String.format("(%d, ", (long)pt.x);
		} else {
			formatted += String.format("(%f, ", pt.x);
		}
		if (pt.y == (long)pt.y) {
			formatted += String.format("%d)", (long)pt.y);
		} else {
			formatted += String.format("%f)", pt.y);
		}
		return formatted;
	}
}
