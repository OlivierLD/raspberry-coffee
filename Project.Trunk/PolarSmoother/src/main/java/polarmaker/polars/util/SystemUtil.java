package polarmaker.polars.util;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class SystemUtil {
	public static double[] solveSystem(double[] m,
	                                   double[] c) {
		SquareMatrix sma = new SquareMatrix(c.length);

		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c.length; j++)
				sma.setElementAt(i, j, m[(c.length * i) + j]);
		}
		return solveSystem(sma, c);
	}

	public static double[] solveSystem(SquareMatrix m,
	                                   double[] c) {
		double[] result;
		result = new double[m.getDimension()];

		SquareMatrix inv = MatrixUtil.invert(m);

		// Square * Column
		for (int row = 0; row < m.getDimension(); row++) {
			result[row] = 0.0;
			for (int col = 0; col < m.getDimension(); col++) {
				result[row] += (inv.getElementAt(row, col) * c[col]);
			}
		}
		return result;
	}

	public static void log(String mess) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("D:\\temp\\matrix.log"));
			bw.write(mess);
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
