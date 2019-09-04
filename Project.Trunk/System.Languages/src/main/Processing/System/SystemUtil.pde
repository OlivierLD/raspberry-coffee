static class SystemUtil {
  static boolean debug = false;

  static double[] solveSystem(SquareMatrix m,
                                     double[] c) {
    double[] result;
    result = new double[m.getDimension()];

    SquareMatrix inv = MatrixUtil.invert(m);

    // Print inverted Matrix
    if (debug) {
      println("Inverted:");
      MatrixUtil.printMatrix(inv);
    }

    // Lines * Column
    for (int row = 0; row < m.getDimension(); row++) {
      result[row] = 0.0;
      for (int col = 0; col < m.getDimension(); col++)
        result[row] += (inv.getElementAt(row, col) * c[col]);
    }
    return result;
  }

  static void printSystem(SquareMatrix squareMatrix, double[] constants) {
    String unknowns = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    int dimension = squareMatrix.getDimension();
    for (int row=0; row<dimension; row++) {
      String line = "";
      for (int col=0; col<dimension; col++) {
        line += String.format("%s(%f x %c)", (!line.trim().isEmpty() ? " + " : ""), squareMatrix.getElementAt(row, col), unknowns.charAt(col));
      }
      line += String.format(" = %f", constants[row]);
      println(line);
    }
  }
}
