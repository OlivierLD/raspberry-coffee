package matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolynomialUtil {

	private final static double PRECISION = 1E-15; // Ca ira...

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

	// Equation solving method
	private static List<Double> getRoots(double... coef) {
		List<Double> roots = new ArrayList<>();

		// Constant function
		if (coef.length == 0) {
			return roots;
		}

		// Linear function
		if (coef.length == 1) {
			roots.add(-coef[0]);
			return roots;
		}

		// One of its root is 0
		if (coef[coef.length - 1] == 0) {
			double[] newcoef = Arrays.copyOfRange(coef, 0, coef.length - 1);
			roots.addAll(getRoots(newcoef));
			roots.add(0.0);
			return roots;
		}

		// Get derivative
		double[] newCoef = Arrays.copyOfRange(coef, 0, coef.length - 1);
		for (int i = 0; i < coef.length - 1; i++) {
			newCoef[i] *= (coef.length - 1 - i) / (double) coef.length;
		}

		// Get root of derivative
		List<Double> rootsA = getRoots(newCoef);
		rootsA.sort((o1, o2) -> o1.compareTo(o2));

		// Get extreme points
		int n = rootsA.size();
		if (n == 0 && coef.length % 2 == 0) {
			return new ArrayList<>();
		} else if (n == 0 && coef.length % 2 == 1) {
			roots.add(approximate(0, coef));
			return roots;
		}

		// There must be a unique root in an open interval if the signs of both ends are different
		// by Intermediate Value Theorem
		// There are n+1 open intervals, and each interval can have one or zero root.
		// Find root in each interval
		double[] x = new double[n];
		double[] fx = new double[n];
		for (int i = 0; i < n; i++) {
			x[i] = rootsA.get(i);
			fx[i] = fx(x[i], coef);
		}

		if (fx[n - 1] <= 0) {
			if (fx[n - 1] == 0) {
				roots.add(x[n - 1]);
			} else {
				roots.add(approximate(x[n - 1] + 1, coef));
			}
		}
		for (int i = n - 2; i >= 0; i--) {
			if (fx[i] * fx[i + 1] <= 0) {
				if (fx[i] == 0) {
					roots.add(x[i]);
				} else {
					roots.add(approximate((x[i] + x[i + 1]) / 2, coef));
				}
			}
		}
		if (fx[0] * (coef.length % 2 == 0 ? -1 : 1) >= 0) {
			if (fx[0] == 0) {
				roots.add(x[0]);
			} else {
				roots.add(approximate(x[0] - 1, coef));
			}
		}
		return roots;
	}

	// Returns function value
	private static double fx(double x, double... coef) {
		double res = 1;
		for (int i = 0; i < coef.length; i++) {
			res *= x;
			res += coef[i];
		}
		return res;
	}

	// Returns differential coefficient
	private static double fpx(double x, double... coef) {
		double res = coef.length;

		for (int i = 0; i < coef.length - 1; i++) {
			res *= x;
			res += coef[i] * (coef.length - 1 - i);
		}
		return res;
	}

	// Returns one of approximated root
	private static double approximate(double xn, double... coef) {
		try {
			double fx = fx(xn, coef);
			double fpx = fpx(xn, coef);

			double xn1 = fx / fpx;
			xn1 = xn - xn1;
			if (Math.abs(xn1 - xn) < PRECISION) {
				return xn1;
			}
			return approximate(xn1, coef);
		} catch (StackOverflowError e) {
			return xn;
		}
	}

	/**
	 *
	 * First this program receives coefficients.
	 * Then it divides all the coefficients with the coefficient of the highest degree.
	 *
	 * Then this gets the derivative of the given equation
	 * until this gets the linear equation.
	 *
	 * With the root of linear equation, this separates the set of real number
	 * into two parts.
	 * Then this approximates the root of primitive equation (quadratic)
	 *
	 * With the root of quadratic equation, this separates the set of real number
	 * into three parts.
	 * Then this approximates the root of primitive equation (cubic)
	 *
	 * (some repetitions...)
	 *
	 * With the root of (n-1)th degree equation, this separates the set of real number
	 * into n parts.
	 * Then this approximate the root of the original equation (nth degree)
	 *
	 * @param coeff highest degree first
	 * @return
	 */
	public static List<Double> getPolynomialRoots(double[] coeff) {
		double[] pCoeff = new double[coeff.length - 1];
		assert(coeff.length > 0 && coeff[0] != 0);
		for (int i=1; i<coeff.length; i++) {
			pCoeff[i-1] = coeff[i] / coeff[0];
		}
		List<Double> roots = new ArrayList<>();

		List<Double> l = getRoots(pCoeff);
		while (l.size() != 0) {
			// There are roots!
			roots.addAll(l);

			// Check if there are more roots
			// Use all roots to factorize the given equation
			int ls = l.size();
			double[] tmp = new double[pCoeff.length - ls];
			for (double r : l) {
				pCoeff[0] += r;
				for (int i = 1; i < pCoeff.length; i++) {
					pCoeff[i] += pCoeff[i - 1] * r;
				}
			}
			// Use new coefficients for more roots
			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = pCoeff[i];
			}
			pCoeff = tmp;
			l = getRoots(pCoeff);
		}
		return roots;
	}

	/**
	 * Add two polynomials
	 * @param a highest degree first
	 * @param b highest degree first
	 * @return
	 */
	public static double[] add(double[] a, double[] b) {
		int dim = Math.max(a.length, b.length);
		double[] sum = new double[dim];
		for (int i=0; i<dim; i++) {
			int aIdx = a.length - (dim - i);
			int bIdx = b.length - (dim - i);
		  sum[i] = (aIdx < 0 ? 0 : a[aIdx]) + (bIdx < 0 ? 0 : b[bIdx]);
		}
		return sum;
	}

	/**
	 * Multiply two polynomials.
	 * @param a highest degree first
	 * @param b highest degree first
	 * @return
	 */
	public static double[] multiply(double[] a, double[] b) {
		int productDim = a.length + b.length - 1;
		double[] product = new double[productDim];
		// init with 0
		for (int i=0; i<productDim; i++) {
			product[i] = 0d;
		}
		for (int x=0; x<a.length; x++) {
			for (int y=0; y<b.length; y++) {
				double prod = a[x] * b[y];
				int posInProd = productDim - (a.length - x + b.length - y) + 1;
				product[posInProd] += prod;
			}
		}
		return product;
	}

	/**
	 * Derivative of a polynomial function
	 * @param coeff highest degree first
	 * @return derivative's coeffs.
	 */
	public static double[] derivative(double[] coeff) {
		int dim = coeff.length - 1;
		double derCoeff[] = new double[dim];
		for (int i=0; i<dim; i++) {
			derCoeff[i] = (dim - i) * coeff[i];
		}
		return derCoeff;
	}

	/**
	 * y = f(x)
	 * @param curveCoeff highest degree first
	 * @param x
	 * @return f(x)
	 */
	public static double f(double curveCoeff[], double x) {
		double y = 0;
		for (int degree=0; degree<curveCoeff.length; degree++) {
			y += (curveCoeff[degree] * Math.pow(x, curveCoeff.length - 1 - degree));
		}
		return y;
	}

	/**
	 * Remove heading monomials with coeff 0
	 * @param p
	 * @return
	 */
	public static double[] reduce(double[] p) {
		if (p[0] != 0) {
			return p;
		} else {
			int firstNonZero = 0;
			while (p[firstNonZero] == 0 && firstNonZero < (p.length - 1)) {
				firstNonZero++;
			}
			if (firstNonZero == p.length - 1) {
				throw new RuntimeException("All coefficients are 0");
			} else {
				int newDim = p.length - firstNonZero;
				double[] newPoly = new double[newDim];
				for (int i=0; i<newDim; i++) {
					newPoly[i] = p[i + firstNonZero];
				}
				return newPoly;
			}
		}
	}

	/**
	 * Distance between a point and a curve fior a given abscissa.
	 * @param coeff curve's coeffs, highest degree first
	 * @param x abscissa
	 * @param pt point
	 * @return
	 */
	public static double dist(double[] coeff, double x, Point pt) {
		double y = f(reduce(coeff), x);
		return Math.sqrt(Math.pow(x - pt.x, 2) + Math.pow(y - pt.y, 2));
	}

	public static String display(double[] p) {
		String display = "";
		for (int i=0; i<p.length; i++) {
			if (p[i] == (long)p[i]) {
				display += (String.format("(%+d%s) %s", (long)p[i], (i == (p.length - 1) ? "" : (i == (p.length - 2) ? " * x" : String.format(" * x^%d", (p.length - 1 - i)))), ((i == (p.length - 1) ? "" : "+ "))));
			} else {
				display += (String.format("(%+f%s) %s", p[i], (i == (p.length - 1) ? "" : (i == (p.length - 2) ? " * x" : String.format(" * x^%d", (p.length - 1 - i)))), ((i == (p.length - 1) ? "" : "+ "))));
			}
		}
		return display;
	}

	public static void main(String... args) {
		double[] polynomial = new double[] { -1, -1, 12.34, 6 };
		System.out.println(String.format("Roots of %s:", display(polynomial)));
		List<Double> roots = getPolynomialRoots(reduce(polynomial));
		if (roots.size() == 0) {
			System.out.println("no root");
		} else {
			System.out.println("roots:");
			for (double r : roots) {
				System.out.println("\t" + r + " => " + f(polynomial, r));
			}
		}
		// Test add
		double[] added = add(
				new double[] { 1, 2, 3, 4, 5 },
				new double[]       { 1, 2, 3 }
		);
		System.out.println("Adding: " + display(added));
		// Test multiply
		double[] multiplied = multiply(
				new double[] { 6, 10, 0, 5 },
				new double[] { 4, 2, 1 }
		);
		System.out.println("Multiplying: " + display(multiplied));
		double[] multiplyBy = multiply(
				new double[] { 6, 10, 0, 5 },
				new double[] { 2 }
		);
		System.out.println("Multiply by: " + display(multiplyBy));
		// Reduce
		System.out.println("Reduced: " + display(reduce(new double[] { 0, 1, 2, 3 })));
		try {
			System.out.println("Reduced: " + display(reduce(new double[] { 0, 0, 0, 0 }))); // Throws Exception
		} catch (Exception ex) {
			System.err.println("As expected:");
			ex.printStackTrace();
		}
	}
}
