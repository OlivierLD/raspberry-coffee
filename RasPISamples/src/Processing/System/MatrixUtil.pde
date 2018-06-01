static class MatrixUtil {
  
  static boolean debug = false;

  static void printMatrix(SquareMatrix m) {
    printMatrix(m, true);
  }

  static void printMatrix(SquareMatrix m, boolean withCR) {
    for (int row=0; row<m.getDimension(); row++) {
      String line = "| ";
      for (int col=0; col<m.getDimension(); col++) {
        line += (m.getElementAt(row, col) + " ");
      }
      line += " |";
      if (!withCR && row == (m.getDimension() - 1)) { // Last line
        print(line);
      } else {
        println(line);
      }
    }
  }

  static SquareMatrix minor(SquareMatrix m, int row, int col) {
    SquareMatrix small = new SquareMatrix(m.getDimension() - 1);
    for (int c = 0; c < m.getDimension(); c++) {
      if (c != col) {
        for (int r = 0; r < m.getDimension(); r++) {
          if (r != row) {
            small.setElementAt(((r < row) ? r : (r - 1)), ((c < col) ? c : (c - 1)), m.getElementAt(r, c));
          }
        }
      }
    }
    return small;
  }

  static SquareMatrix comatrix(SquareMatrix m) {
    SquareMatrix co = new SquareMatrix(m.getDimension());
    for (int r = 0; r < m.getDimension(); r++) {
      for (int c = 0; c < m.getDimension(); c++) {
        co.setElementAt(r, c, determinant(minor(m, r, c)) * Math.pow((-1), (r + c + 2)));  // r+c+2 = (r+1) + (c+1)...
      }
    }
    if (debug) {
      println("Comatrix:");
      printMatrix(co);
    }
    return co;
  }

  static SquareMatrix transposed(SquareMatrix m) {
    SquareMatrix t = new SquareMatrix(m.getDimension());
    // Replace line with columns.
    int r, c;
    for (r = 0; r < m.getDimension(); r++) {
      for (c = 0; c < m.getDimension(); c++) {
        t.setElementAt(r, c, m.getElementAt(c, r));
      }
    }
    if (debug) {
      println("Transposed:");
      printMatrix(t);
    }
    return t;
  }

 static SquareMatrix multiply(SquareMatrix m, double n) {
    SquareMatrix res = new SquareMatrix(m.getDimension());
    int r, c;

    for (r = 0; r < m.getDimension(); r++) {
      for (c = 0; c < m.getDimension(); c++) {
        res.setElementAt(r, c, m.getElementAt(r, c) * n);
      }
    }
    return res;
  }

  static boolean equals(SquareMatrix a, SquareMatrix b) {
    if (a.getDimension() != b.getDimension()) {
      return false;
    }
    for (int r=0; r<a.getDimension(); r++) {
      for (int c=0; c<a.getDimension(); c++) {
        if (a.getElementAt(r, c) != b.getElementAt(r, c)) {
          return false;
        }
      }
    }
    return true;
  }

  static double determinant(SquareMatrix m) {
    double v = 0.0;

    if (m.getDimension() == 1) {
      v = m.getElementAt(0, 0);
    } else {
      // C : column in Major
      for (int C = 0; C < m.getDimension(); C++) { // Walk thru first line
        // Minor's determinant
        double minDet = determinant(minor(m, 0, C));
        v += (m.getElementAt(0, C) * minDet * Math.pow((-1.0), C + 1 + 1)); // line C, column 1
      }
    }
    if (debug) {
      println("Determinant of");
      printMatrix(m, false);
      println(String.format(" is %f", v));
    }
    return v;
  }

  static SquareMatrix invert(SquareMatrix m) {
    return multiply(transposed(comatrix(m)), (1.0 / determinant(m)));
  }
}
