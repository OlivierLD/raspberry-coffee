package matrix;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.*;

public class MatrixTests {

	@Before
	public void setup() {
//	System.out.println("C'est parti!");
		System.setProperty("debug", "false");
	}

	@Test
	public void identity2x2() {
		SquareMatrix matrix = new SquareMatrix(2, 1, 0, 0, 1);
		double determinant = MatrixUtil.determinant(matrix);
		assertEquals("Determinant should be equal to 1", 1.0, determinant);
	}

	@Test
	public void biggerOne() {
		SquareMatrix matrix = new SquareMatrix(3, 12, 13, 14, 1.345, -654, 0.001, 23.09, 5.3, -12.34);
		double determinant = MatrixUtil.determinant(matrix);
//	System.out.println(String.format("Determinant: %f", determinant));
		assertEquals("Determinant should be equal to 308572.160470", 308572.160470, determinant);
	}

	@Test
	public void inversion() {
		SquareMatrix matrix = new SquareMatrix(3, 12, 13, 14, 1.345, -654, 0.001, 23.09, 5.3, -12.34);
		SquareMatrix inverted  = MatrixUtil.invert(matrix);
		SquareMatrix expected = new SquareMatrix(3,
				0.026153865234335084, 7.60340789145203E-4, 0.029672193972567294,
				5.386224724448487E-5, -0.0015274871177039465, 6.0984114611433074E-5,
				0.04896095771241434, 7.666602186006338E-4, -0.025489937225768294);
//	MatrixUtil.printMatrix(inverted);
		assertTrue("Matrixes should be equal.", MatrixUtil.equals(inverted, expected));
	}

	@Test
	public void system() {
		SquareMatrix matrix = new SquareMatrix(3, 12, 13, 14, 1.345, -654, 0.001, 23.09, 5.3, -12.34);
		double[] constants = new double[]{234, 98.87, 9.876};
		double[] result = SystemUtil.solveSystem(matrix, constants);
//		System.out.println("A = " + result[0]);
//		System.out.println("B = " + result[1]);
//		System.out.println("C = " + result[2]);
		assertEquals("3 Coeffs", result.length, 3);
		assertEquals("Coeff A", 6.48822194633027, result[0]);
		assertEquals("Coeff B", -0.13781660635627724, result[1]);
		assertEquals("Coeff C", 11.280925180476313, result[2]);
	}

}
