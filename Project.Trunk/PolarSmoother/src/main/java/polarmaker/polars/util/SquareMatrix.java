package polarmaker.polars.util;

public final class SquareMatrix {
	private int dimension;
	private double[][] matrixElements;

	public SquareMatrix() {
	}

	public SquareMatrix(int n) {
		this.dimension = n;
		matrixElements = new double[n][n];
	}

	public void setDimension(int dim) {
		this.dimension = dim;
		matrixElements = new double[dim][dim];
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

	public double[][] getmatrixElements() {
		return this.matrixElements;
	}

	public void setmatrixElements(double[][] me) {
		this.matrixElements = me;
	}
}
