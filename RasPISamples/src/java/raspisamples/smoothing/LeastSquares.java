package raspisamples.smoothing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import raspisamples.matrix.SquareMatrix;
import raspisamples.matrix.SystemUtil;

/**
 * For details on the least squares method:
 * See http://www.efunda.com/math/leastsquares/leastsquares.cfm
 * See http://www.lediouris.net/original/sailing/PolarCO2/index.html
 */
public class LeastSquares {

	private static double f(double x, double... coeffs) {
		double result = 0.0;
		for (int deg=0; deg<coeffs.length; deg++) {
			result += (coeffs[deg] * Math.pow(x, coeffs.length - (deg + 1)));
		}
		return result;
	}

	public static void cloudGenerator(BufferedWriter bw, double fromX, double toX, double step, double yTolerance, int iterations, double... coeffs) {
		for (int i=0; i<iterations; i++) {
			for (double x=fromX; x<=toX; x+=step) {
				double y = f(x, coeffs) + (yTolerance * ((2 * Math.random()) - 1));
				try {
					bw.write(String.format("%f;%f\n", x, y));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static class Tuple {
		double x, y;
		public Tuple(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

	public static void main(String... args) throws IOException {
//		double y = f(10, -0.0061, 0.0029, 4.6);
//		System.out.println(String.format("F(%f)=%f", 10f, y));

		if (false) { // Turn to true to re-generate data
			BufferedWriter bw = new BufferedWriter(new FileWriter("cloud.csv"));
			cloudGenerator(bw, 0, 50, 0.01, 5, 1, -0.0061, 0.0029, 4.6);
			bw.close();
		}

		List<Tuple> data = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader("cloud.csv"));
		String line = "";
		boolean go = true;
		while (go) {
			line = br.readLine();
			if (line == null) {
				go = false;
			} else {
				String[] tuple = line.split(";");
				data.add(new Tuple(Double.parseDouble(tuple[0]), Double.parseDouble(tuple[1])));
			}
		}
		br.close();

		// Data is a list of Tuples
		int requiredDegree = 2;
		int dimension = requiredDegree + 1;
		double[] sumXArray = new double[(requiredDegree * 2) + 1]; // Will fill the matrix
		double[] sumY      = new double[requiredDegree + 1];
		// Init
		for (int i=0; i<((requiredDegree * 2) + 1); i++)
			sumXArray[i] = 0.0;
		for (int i=0; i<(requiredDegree + 1); i++)
			sumY[i] = 0.0;

		data.stream().forEach(tuple -> {
			for (int i=0; i<((requiredDegree * 2) + 1); i++)
				sumXArray[i] += Math.pow(tuple.x, i);
			for (int i=0; i<(requiredDegree + 1); i++)
				sumY[i] += (tuple.y * Math.pow(tuple.x, i));
		});

		SquareMatrix squareMatrix = new SquareMatrix(dimension);
		for (int row=0; row<dimension; row++) {
			for (int col=0; col<dimension; col++) {
				int powerRnk = (requiredDegree - row) + (requiredDegree - col);
				System.out.println("[" + row + "," + col + ":" + (powerRnk) + "] = " + sumXArray[powerRnk]);
				squareMatrix.setElementAt(row, col, sumXArray[powerRnk]);
			}
		}
		double[] constants = new double[dimension];
		for (int i=0; i<dimension; i++) {
			constants[i] = sumY[requiredDegree - i];
			System.out.println("[" + (requiredDegree - i) + "] = " + constants[i]);
		}

		System.out.println("Resolving:");
		SystemUtil.printSystem(squareMatrix, constants);
		System.out.println();

		double[] result = SystemUtil.solveSystem(squareMatrix, constants);
		for (int i=0; i<result.length; i++) {
			System.out.println(String.format("%f", result[i]));
		}
		// Nicer (Java 8)
		System.out.println();
		AtomicInteger integer = new AtomicInteger(0);
		Arrays.stream(result)
						.boxed()
						.map(coef -> new IndexedCoeff(integer.incrementAndGet(), coef))
						.forEach(ic -> System.out.println(String.format("Deg %d -> %f", (dimension - ic.idx), ic.coef)));
	}

	private static class IndexedCoeff {
		int idx;
		double coef;
		public IndexedCoeff(int idx, double coef) {
			this.idx = idx;
			this.coef = coef;
		}
	}
}
