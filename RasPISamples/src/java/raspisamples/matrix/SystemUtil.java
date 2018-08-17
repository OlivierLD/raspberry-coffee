package raspisamples.matrix;

import java.text.DecimalFormat;
import java.util.Arrays;

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
		int dimension = squareMatrix.getDimension();
		for (int row=0; row<dimension; row++) {
			String line = "";
			for (int col=0; col<dimension; col++) {
				line += String.format("%s(%f x %c)", (line.trim().length() > 0 ? " + " : ""), squareMatrix.getElementAt(row, col), unknowns.charAt(col));
			}
			line += String.format(" = %f", constants[row]);
			System.out.println(line);
		}
	}

	public static double[] derivative(double[] coeff) {
		int dim = coeff.length - 1;
		double derCoeff[] = new double[dim];
		for (int i=0; i<dim; i++) {
			derCoeff[i] = (dim - i) * coeff[i];
		}
		return derCoeff;
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
		System.out.println(String.format("\nDone in %s \u212bs (nano).", DecimalFormat.getInstance().format(after - before)));

		System.out.println(String.format("A = %f", result[0]));
		System.out.println(String.format("B = %f", result[1]));
		System.out.println(String.format("C = %f", result[2]));

		// Test derivative
		double[] der = derivative(new double[] { 3d, 2d, 1d, 6d});
		Arrays.stream(der).forEach(c -> System.out.print(String.format("%f ", c)));
		System.out.println();
	}
}
