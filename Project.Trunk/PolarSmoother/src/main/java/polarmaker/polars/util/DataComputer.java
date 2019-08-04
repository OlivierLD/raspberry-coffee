package polarmaker.polars.util;

import polarmaker.polars.PolarPoint;

import java.util.Enumeration;
import java.util.Vector;

public class DataComputer {
	/**
	 * Smoothes a curve with the least squares method
	 *
	 * @param pp        Vector of PolarPoint
	 * @param reqDegree degree of the expected smoothed polynom
	 * @return the array of smoothed coefficients
	 */
	public static double[] smooth(Vector pp, int reqDegree) {
		// Calculation of the terms in the matrix
		int dimension = reqDegree + 1;
		double[] sumXArray = new double[(reqDegree * 2) + 1];
		double[] sumY = new double[reqDegree + 1];
		for (int i = 0; i < ((reqDegree * 2) + 1); i++) {
			sumXArray[i] = 0.0;
		}
		for (int i = 0; i < (reqDegree + 1); i++) {
			sumY[i] = 0.0;
		}
		Enumeration en = pp.elements();

//  int counter = 0;
		while (en.hasMoreElements() /* && (counter++ < 300) */) {
			PolarPoint aPP = (PolarPoint) en.nextElement();
			double wa = aPP.getTwa(); // X
			double bs = aPP.getBsp(); // Y
			for (int i = 0; i < ((reqDegree * 2) + 1); i++) {
				sumXArray[i] += Math.pow(wa, i);
			}
			for (int i = 0; i < (reqDegree + 1); i++) {
				sumY[i] += (bs * Math.pow(wa, i));
			}
		}
		SquareMatrix sm = new SquareMatrix(dimension);
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				int powerRnk = (reqDegree - i) + (reqDegree - j);
				//  System.out.println("[" + i + "," + j + ":" + (powerRnk) + "] = " + sumXArray[powerRnk]);
				//  sm.setElementAt(i, j, 2 * sumXArray[powerRnk]);
				sm.setElementAt(i, j, sumXArray[powerRnk]);
			}
		}
		double[] coeff = new double[dimension];
		for (int i = 0; i < dimension; i++) {
			coeff[i] = sumY[reqDegree - i];
			// System.out.println("[" + (reqDegree - i) + "] = " + coeff[i]);
		}

		// Display matrix
    /*
    for (int i=0; i<dimension; i++)
    {
      System.out.print("|\t");
      for (int j=0; j<dimension; j++)
      {
        System.out.print(sm.getElementAt(i, j) + "\t");
      }
      System.out.println("\t|\t|\t" + coeff[i] + "\t|");
    }
    */
		double[] result = SystemUtil.solveSystem(sm, coeff);

//  for (int i=0; i<result.length; i++)
//    System.out.println("Degree " + (reqDegree - i) + " = " + result[i]);

		return result;
	}
}
