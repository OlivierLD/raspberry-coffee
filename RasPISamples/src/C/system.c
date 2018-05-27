/*
 * Compile with
 * gcc -o system system.c
 ***********************************************************************
 * Oliv proudly did it.
 * 
 * TODO Free resources.
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <float.h>
#include <string.h>

#define TRUE 1
#define FALSE 0

#define DEBUG FALSE

double * minor(int dim, double * matrix, int row, int col) {
	double * small = (double *) malloc((dim - 1) * (dim - 1) * sizeof(double));
	for (int c=0; c<dim; c++) {
		if (c != col) {
			for (int r=0; r<dim; r++) {
				if (r != row) {
					small[(((r < row) ? r : (r - 1)) * (dim - 1)) + ((c < col) ? c : (c - 1))] = matrix[(r * dim) + c];
				}
			}
		}
	}
	return small;
}

double determinant(int dim, double * matrix) {
	double v = 0;
	if (dim == 1) {
		v = matrix[0];
	} else {
		// C: column in major
		for (int C=0; C<dim; C++) { // Walk thru first line
			double minDet = determinant(dim - 1, minor(dim, matrix, 0, C));
			v += (matrix[C] * minDet * pow(-1, C + 1 + 1));
		}
	}
	return v;
}

double * comatrix(int dim, double * matrix) {
	double * comat = (double *) malloc(dim * dim * sizeof(double));
	for (int r=0; r<dim; r++) {
		for (int c=0; c<dim; c++) {
			comat[(r * dim) + c] = determinant((dim - 1), minor(dim, matrix, r, c)) * pow(-1, (r + c + 2));
		}
	}
	return comat;
}

double * transposed(int dim, double * matrix) {
	double * transp = (double *) malloc(dim * dim * sizeof(double));
	for (int r=0; r<dim; r++) {
		for (int c=0; c<dim; c++) {
			transp[(r * dim) + c] = matrix[(c * dim) + r];
		}
	}
	return transp;
}

double * multiply(int dim, double * matrix, double n) {
	double * mult = (double *) malloc(dim * dim * sizeof(double));
	for (int r=0; r<dim; r++) {
		for (int c=0; c<dim; c++) {
			mult[(r * dim) + c] = matrix[(r * dim) + c] * n;
		}
	}
	return mult;
}

double * invert(int dim, double * matrix) {
	return multiply(dim, transposed(dim, comatrix(dim, matrix)), (1.0 / determinant(dim, matrix)));
}

void printMatrix(int dim, double * matrix) {
	for (int r=0; r<dim; r++) {
		fprintf(stdout, "| ");
		for (int c=0; c<dim; c++) {
			fprintf(stdout, "%lf ", matrix[(r * dim) + c]);
		}
		fprintf(stdout, " |\n");
	}
}

double * solveSystem(int dim, double * matrix, double * coeff) {
	double * result = (double *) malloc(dim * sizeof(double));
	double * inverted = invert(dim, matrix);

	if (DEBUG) {
		fprintf(stdout, "Inverted:\n");
		printMatrix(3, inverted);
	}

	for (int r=0; r<dim; r++) {
		result[r] = 0.0;
		for (int c=0; c<dim; c++) {
			result[r] += (inverted[(r * dim) + c] * coeff[c]);
		}
	}
	return result;
}

void printSystem(int dim, double * matrix, double * coeff) {
	char * unknowns = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	for (int r=0; r<dim; r++) {
		for (int c=0; c<dim; c++) {
			fprintf(stdout, "%s(%lf x %c)", (c==0?"":" + "), matrix[(r * dim) +c], unknowns[c]);
		}
		fprintf(stdout, " = %lf\n", coeff[r]);
	}
}

int main(int argc, char **argv) {
	double matrix[9] = {
		 12,       13,      14,
		  1.345, -654,       0.001,
		 23.09,     5.3, -12.34
	};
	double constants[3] = { 234, 98.87, 9.876 };

	fprintf(stdout, "Solving system:\n");
	printSystem(3, &matrix[0], &constants[0]);
	double * result = solveSystem(3, &matrix[0], &constants[0]);
	fprintf(stdout, "A: %lf\n", result[0]);
	fprintf(stdout, "B: %lf\n", result[1]);
	fprintf(stdout, "C: %lf\n", result[2]);
}
