package matrix;

public final class SquareMatrix {
	private int dimension;
	private double[][] matrixElements;

	public SquareMatrix(int dim) {
		this(dim, false);
	}

	public SquareMatrix(int dim, boolean init) {
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

	public SquareMatrix(int dim, double... elements) {
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

	public static SquareMatrix builder(int dim) {
		return new SquareMatrix(dim);
	}

	public int getDimension() {
		return (this.dimension);
	}

	public void setElementAt(int row, int col, double val) {
		matrixElements[row][col] = val;
	}

	public double getElementAt(int row, int col) {
		return matrixElements[row][col];
	}

	public double[][] getMatrixElements() {
		return this.matrixElements;
	}

	public void setMatrixElements(double[][] me) {
		this.matrixElements = me;
	}
}
