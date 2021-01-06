package smoothing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import matrix.SquareMatrix;
import matrix.SystemUtil;

/**
 * For details on the least squares method:
 * See http://www.efunda.com/math/leastsquares/leastsquares.cfm
 * See http://www.lediouris.net/original/sailing/PolarCO2/index.html
 */
public class LeastSquaresMethod {

	public static double f(double x, double... coeffs) {
		double result = 0.0;
		for (int deg=0; deg<coeffs.length; deg++) {
			result += (coeffs[deg] * Math.pow(x, coeffs.length - (deg + 1)));
		}
		return result;
	}

	public static void cloudGenerator(BufferedWriter bw, double fromX, double toX, double step, double yTolerance, double... coeffs) {
		cloudGenerator(bw, fromX, toX, step, new double[] { yTolerance }, coeffs);
	}
	public static void cloudGenerator(BufferedWriter bw, double fromX, double toX, double step, double[] yTolerance, double... coeffs) {
		double miny = Double.MAX_VALUE, maxy = -Double.MAX_VALUE;
		for (int i=0; i<yTolerance.length; i++) {
			for (double x=fromX; x<=toX; x+=step) {
				double y = f(x, coeffs) + ((yTolerance[i]) * ((2 * Math.random()) - 1));
				miny = Math.min(miny, y);
				maxy = Math.max(maxy, y);
				try {
					bw.write(String.format("%f;%f\n", x, y));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(String.format("Y in [%f, %f]", miny, maxy));
	}

	public static void csvToJson(String csvName, String jsonName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(csvName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(jsonName));
		String line = "";
		bw.write("[");
		boolean go = true, first = true;
		while (go) {
			line = br.readLine();
			if (line == null) {
				go = false;
			} else {
				String[] tuple = line.split(";");
				String jSonLine = String.format("%s{ \"x\": %f, \"y\": %f }", (first ? "" : ", "), Double.parseDouble(tuple[0]), Double.parseDouble(tuple[1]));
				bw.write(jSonLine);
				first = false;
			}
		}
		bw.write("]");
		bw.close();
		br.close();

	}

	public static class Tuple {
		double x, y;
		public Tuple(double x, double y) {
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

	public static void main(String... args) throws IOException {
//		double y = f(10, -0.0061, 0.0029, 4.6);
//		System.out.println(String.format("F(%f)=%f", 10f, y));

		final int REQUIRED_SMOOTHING_DEGREE = 3;

		if (true) { // Turn to true to re-generate data
			BufferedWriter bw = new BufferedWriter(new FileWriter("cloud.csv"));
			cloudGenerator(bw, -8, 8, 0.01, new double[] {3, 4, 5, 6, 9}, 0.01, -0.04, 0.2, 1);
			bw.close();
		}

		if (true) {
			csvToJson("cloud.csv", "cloud.json");
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
		int dimension = REQUIRED_SMOOTHING_DEGREE + 1;
		double[] sumXArray = new double[(REQUIRED_SMOOTHING_DEGREE * 2) + 1]; // Will fill the matrix
		double[] sumY      = new double[REQUIRED_SMOOTHING_DEGREE + 1];
		// Init
		for (int i=0; i<((REQUIRED_SMOOTHING_DEGREE * 2) + 1); i++) {
			sumXArray[i] = 0.0;
		}
		for (int i=0; i<(REQUIRED_SMOOTHING_DEGREE + 1); i++) {
			sumY[i] = 0.0;
		}

		data.stream().forEach(tuple -> {
			for (int i=0; i<((REQUIRED_SMOOTHING_DEGREE * 2) + 1); i++) {
				sumXArray[i] += Math.pow(tuple.x, i);
			}
			for (int i=0; i<(REQUIRED_SMOOTHING_DEGREE + 1); i++) {
				sumY[i] += (tuple.y * Math.pow(tuple.x, i));
			}
		});

		SquareMatrix squareMatrix = new SquareMatrix(dimension);
		for (int row=0; row<dimension; row++) {
			for (int col=0; col<dimension; col++) {
				int powerRnk = (REQUIRED_SMOOTHING_DEGREE - row) + (REQUIRED_SMOOTHING_DEGREE - col);
				System.out.println("[" + row + "," + col + ":" + (powerRnk) + "] = " + sumXArray[powerRnk]);
				squareMatrix.setElementAt(row, col, sumXArray[powerRnk]);
			}
		}
		double[] constants = new double[dimension];
		for (int i=0; i<dimension; i++) {
			constants[i] = sumY[REQUIRED_SMOOTHING_DEGREE - i];
			System.out.println("[" + (REQUIRED_SMOOTHING_DEGREE - i) + "] = " + constants[i]);
		}

		System.out.println("Resolving:");
		SystemUtil.printSystem(squareMatrix, constants);
		System.out.println();

		double[] result = SystemUtil.solveSystem(squareMatrix, constants);
		String out = "[ ";
		for (int i=0; i<result.length; i++) {
			out += String.format("%s%f", (i > 0 ? ", " : ""), result[i]);
		}
		out += " ]";
		System.out.println(out);
		// Nicer (Java 8 and after)
		System.out.println();
		AtomicInteger integer = new AtomicInteger(0);
		Arrays.stream(result)
						.boxed()
						.map(coef -> new IndexedCoeff(integer.incrementAndGet(), coef))
						.forEach(ic -> System.out.println(String.format("Deg %d -> %f", (dimension - ic.idx), ic.coef)));
	}

	public static class IndexedCoeff {
		int idx;
		double coef;
		public IndexedCoeff(int idx, double coef) {
			this.idx = idx;
			this.coef = coef;
		}

		public int getIdx() {
			return idx;
		}
		public double getCoef() {
			return coef;
		}
	}
}
