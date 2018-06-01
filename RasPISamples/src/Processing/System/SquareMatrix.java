// Object definition
class SquareMatrix {
  private int dimension;
  private double[][] matrixElements;

  SquareMatrix(int dim) {
    this(dim, false);
  }

  SquareMatrix(int dim, boolean init) {
    if (dim < 1) {
      throw new IllegalArgumentException("Dimension must be at least 1");
    }
    this.dimension = dim;
    matrixElements = new double[dim][dim];
    if (init) {
      for (int l = 0; l < dim; l++) {
        for (int c = 0; c < dim; c++)
          matrixElements[l][c] = 0d;
      }
    }
  }

  SquareMatrix(int dim, double... elements) {
    this(dim);
    if (elements == null) {
      throw new IllegalArgumentException("Elements array cannot be null");
    }
    if (elements.length != (dim * dim)) {
      throw new IllegalArgumentException(String.format("Invalid number of elements for a matrix of dim %d, expecting %d, got %d", dim, (dim * dim), elements.length));
    }
    for (int l = 0; l < dim; l++) {
      for (int c = 0; c < dim; c++)
        matrixElements[l][c] = elements[(l * dim) + c];
    }
  }

  static SquareMatrix builder(int dim) {
    return new SquareMatrix(dim);
  }

  int getDimension() {
    return (this.dimension);
  }

  void setElementAt(int row, int col, double val) {
    matrixElements[row][col] = val;
  }

  double getElementAt(int row, int col) {
    return matrixElements[row][col];
  }

  double[][] getmatrixElements() {
    return this.matrixElements;
  }

  void setmatrixElements(double[][] me) {
    this.matrixElements = me;
  }
}
