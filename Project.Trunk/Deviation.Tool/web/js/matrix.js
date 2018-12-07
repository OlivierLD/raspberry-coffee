if (Math.toRadians === undefined) {
	Math.toRadians = function (deg) {
		return deg * (Math.PI / 180);
	};
}

if (Math.toDegrees === undefined) {
	Math.toDegrees = function (rad) {
		return rad * (180 / Math.PI);
	};
}

/**
 * Utilities for system resolution, least squares, etc.
 * This follows the ES5 standard.
 *
 * @param dim
 * @constructor
 */
function SquareMatrix(dim) {
	var dimension = dim;
	var matrixElements = [];
	for (var row = 0; row < dim; row++) {
		var line = [];
		for (var col = 0; col < dim; col++) {
			line.push(0);
		}
		matrixElements.push(line);
	}

	this.getDimension = function () {
		return (dimension);
	};

	this.setElementAt = function (row, col, val) {
		matrixElements[row][col] = val;
	};

	this.getElementAt = function (row, col) {
		return matrixElements[row][col];
	};

	this.getMatrixElements = function () {
		return this.matrixElements;
	};

	this.setMatrixElements = function (me) {
		this.matrixElements = me;
	};
};

var minor = function (m, row, col) {
	var small = new SquareMatrix(m.getDimension() - 1);
	for (var c = 0; c < m.getDimension(); c++) {
		if (c != col) {
			for (var r = 0; r < m.getDimension(); r++) {
				if (r != row) {
					small.setElementAt(((r < row) ? r : (r - 1)), ((c < col) ? c : (c - 1)), m.getElementAt(r, c));
				}
			}
		}
	}
	return small;
};

var comatrix = function (m) {
	var co = new SquareMatrix(m.getDimension());
	for (var r = 0; r < m.getDimension(); r++) {
		for (var c = 0; c < m.getDimension(); c++) {
			co.setElementAt(r, c, determinant(minor(m, r, c)) * Math.pow((-1), (r + c + 2)));  // r+c+2 = (r+1) + (c+1)...
		}
	}
	return co;
};

var transposed = function (m) {
	var t = new SquareMatrix(m.getDimension());
	// Replace line with columns.
	var r, c;
	for (r = 0; r < m.getDimension(); r++) {
		for (c = 0; c < m.getDimension(); c++) {
			t.setElementAt(r, c, m.getElementAt(c, r));
		}
	}
	return t;
};

var multiply = function (m, n) {
	var res = new SquareMatrix(m.getDimension());
	var r, c;
	for (r = 0; r < m.getDimension(); r++) {
		for (c = 0; c < m.getDimension(); c++) {
			res.setElementAt(r, c, m.getElementAt(r, c) * n);
		}
	}
	return res;
};

var determinant = function (m) {
	var v = 0.0;

	if (m.getDimension() == 1) {
		v = m.getElementAt(0, 0);
	} else {
		// C : column in Major
		for (var C = 0; C < m.getDimension(); C++) { // Walk thru first line
			// Minor's determinant
			var minDet = determinant(minor(m, 0, C));
			v += (m.getElementAt(0, C) * minDet * Math.pow((-1.0), C + 1 + 1)); // line C, column 1
		}
	}
	return v;
};

var invert = function (m) {
	return multiply(transposed(comatrix(m)), (1.0 / determinant(m)));
};

/**
 * Solves a system, n equations, n unknowns.
 * <p>
 * the values we look for are x, y, z.
 * <p>
 * ax + by + cz = X
 * Ax + By + Cz = Y
 * Px + Qy + Rz = Z
 *
 * @param m Coeffs matrix, n x n (left)
 *          | a b c |
 *          | A B C |
 *          | P Q R |
 * @param c Constants array, n (right) [X, Y, Z]
 * @return the unknown array, n. [x, y, z]
 */
var solveSystem = function (m, c) {
	var result = [];

	var inv = invert(m);

	// Lines * Column
	for (var row = 0; row < m.getDimension(); row++) {
		result.push(0.0);
		for (var col = 0; col < m.getDimension(); col++) {
			result[row] += (inv.getElementAt(row, col) * c[col]);
		}
	}
	return result;
};

var printSystem = function (squareMatrix, constants) {
	var unknowns = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	var dimension = squareMatrix.getDimension();
	for (var row = 0; row < dimension; row++) {
		var line = "";
		for (var col = 0; col < dimension; col++) {
			line += ((line.trim().length > 0 ? " + " : "") + squareMatrix.getElementAt(row, col) + " x " + unknowns.charAt(col));
		}
		line += (" = " + constants[row]);
		console.log(line);
	}
};

var f = function (x, coeffs) {
	var result = 0.0;
	for (var deg = 0; deg < coeffs.length; deg++) {
		result += (coeffs[deg] * Math.pow(x, coeffs.length - (deg + 1)));
	}
	return result;
};

var derivative = function (coeff) {
	var dim = coeff.length - 1;
	var derCoeff = [];
	for (var i = 0; i < dim; i++) {
		derCoeff.push((dim - i) * coeff[i]);
	}
	return derCoeff;
};


// Least Squares
var leastSquares = function (requiredDegree, data) {
	var dimension = requiredDegree + 1;
	var sumXArray = [];
	var sumY = [];
// Init
	for (var i = 0; i < ((requiredDegree * 2) + 1); i++) {
		sumXArray.push(0.0);
	}
	for (var i = 0; i < (requiredDegree + 1); i++) {
		sumY.push(0.0);
	}

	for (var t = 0; t < data.length; t++) {
		for (var i = 0; i < ((requiredDegree * 2) + 1); i++) {
			sumXArray[i] += Math.pow(data[t].x, i);
		}
		for (var i = 0; i < (requiredDegree + 1); i++) {
			sumY[i] += (data[t].y * Math.pow(data[t].x, i));
		}
	}

	var squareMatrix = new SquareMatrix(dimension);
	for (var row = 0; row < dimension; row++) {
		for (var col = 0; col < dimension; col++) {
			var powerRnk = (requiredDegree - row) + (requiredDegree - col);
			console.log("[" + row + "," + col + ":" + (powerRnk) + "] = " + sumXArray[powerRnk]);
			squareMatrix.setElementAt(row, col, sumXArray[powerRnk]);
		}
	}
	var constants = []; // new double[dimension];
	for (var i = 0; i < dimension; i++) {
		constants.push(sumY[requiredDegree - i]);
		console.log("[" + (requiredDegree - i) + "] = " + constants[i]);
	}

//  console.log("Resolving:");
//  printSystem(squareMatrix, constants);

	var result = solveSystem(squareMatrix, constants);
	return result;
};

var smoothDevCurve = function (data) {
	var dimension = 5;
	var n = 0, sinR = 0, cosR = 0, sin2R = 0, cos2R = 0,
			sinR2 = 0, sinRcosR = 0, sin2RsinR = 0, cos2RsinR = 0,
			cosR2 = 0, sin2RcosR = 0, cos2RcosR = 0, sin2R2 = 0,
			cos2Rsin2R = 0, cos2R2 = 0;
	var d = 0, dSinR = 0, dCosR = 0, dSin2R = 0, dCos2R = 0;

	data.forEach(point => {
		n += 1;
		sinR += Math.sin(Math.toRadians(point.x));
		cosR += Math.cos(Math.toRadians(point.x));
		sin2R += Math.sin(2 * Math.toRadians(point.x));
		cos2R += Math.cos(2 * Math.toRadians(point.x));
		sinR2 += Math.pow(Math.sin(Math.toRadians(point.x)), 2);
		sinRcosR += (Math.sin(Math.toRadians(point.x)) * Math.cos(Math.toRadians(point.x)));
		sin2RsinR += (Math.sin(2 * Math.toRadians(point.x)) * Math.sin(Math.toRadians(point.x)));
		cos2RsinR += (Math.cos(2 * Math.toRadians(point.x)) * Math.sin(Math.toRadians(point.x)));
		cosR2 += Math.pow(Math.cos(Math.toRadians(point.x)), 2);
		sin2RcosR += (Math.sin(2 * Math.toRadians(point.x)) * Math.cos(Math.toRadians(point.x)));
		cos2RcosR += (Math.cos(2 * Math.toRadians(point.x)) * Math.cos(Math.toRadians(point.x)));
		sin2R2 += Math.pow(Math.sin(2 * Math.toRadians(point.x)), 2);
		cos2Rsin2R += (Math.cos(2 * Math.toRadians(point.x)) * Math.sin(2 * Math.toRadians(point.x)));
		cos2R2 += Math.pow(Math.cos(2 * Math.toRadians(point.x)), 2);

		d += point.y;
		dSinR += (point.y * Math.sin(Math.toRadians(point.x)));
		dCosR += (point.y * Math.cos(Math.toRadians(point.x)));
		dSin2R += (point.y * Math.sin(2 * Math.toRadians(point.x)));
		dCos2R += (point.y * Math.cos(2 * Math.toRadians(point.x)));
	});

	var squareMatrix = new SquareMatrix(dimension);
	// Line 1
	squareMatrix.setElementAt(0, 0, n);
	squareMatrix.setElementAt(0, 1, sinR);
	squareMatrix.setElementAt(0, 2, cosR);
	squareMatrix.setElementAt(0, 3, sin2R);
	squareMatrix.setElementAt(0, 4, cos2R);
	// Line 2
	squareMatrix.setElementAt(1, 0, sinR);
	squareMatrix.setElementAt(1, 1, sinR2);
	squareMatrix.setElementAt(1, 2, sinRcosR);
	squareMatrix.setElementAt(1, 3, sin2RsinR);
	squareMatrix.setElementAt(1, 4, cos2RsinR);
	// Line 3
	squareMatrix.setElementAt(2, 0, cosR);
	squareMatrix.setElementAt(2, 1, sinRcosR);
	squareMatrix.setElementAt(2, 2, cosR2);
	squareMatrix.setElementAt(2, 3, sin2RcosR);
	squareMatrix.setElementAt(2, 4, cos2RcosR);
	// Line 4
	squareMatrix.setElementAt(3, 0, sin2R);
	squareMatrix.setElementAt(3, 1, sin2RsinR);
	squareMatrix.setElementAt(3, 2, sin2RcosR);
	squareMatrix.setElementAt(3, 3, sin2R2);
	squareMatrix.setElementAt(3, 4, cos2Rsin2R);
	// Line 4
	squareMatrix.setElementAt(4, 0, cos2R);
	squareMatrix.setElementAt(4, 1, cos2RsinR);
	squareMatrix.setElementAt(4, 2, cos2RcosR);
	squareMatrix.setElementAt(4, 3, cos2Rsin2R);
	squareMatrix.setElementAt(4, 4, cos2R2);

	var constants = []; // new double[dimension];
	constants.push(d);
	constants.push(dSinR);
	constants.push(dCosR);
	constants.push(dSin2R);
	constants.push(dCos2R);

//  console.log("Resolving:");
//  printSystem(squareMatrix, constants);

	var result = solveSystem(squareMatrix, constants);
	return result;
};

if (false) { // Example
	var REQUIRED_SMOOTHING_DEGREE = 3;
// Cloud of points here:
	var data = [{"x": -8.000000, "y": -6.719560}, {"x": -7.990000, "y": -7.827249}, {
		"x": -7.980000,
		"y": -9.274245
	}, {"x": -7.970000, "y": -8.640282}, {"x": -7.960000, "y": -7.339933}, {
		"x": -7.950000,
		"y": -6.246416
	}, {"x": -7.940000, "y": -9.084759}, {"x": -7.930000, "y": -9.104593}, {
		"x": -7.920000,
		"y": -6.523360
	}, {"x": -7.910000, "y": -5.865572}, {"x": -7.900000, "y": -8.498517}, {
		"x": -7.890000,
		"y": -5.992720
	}, {"x": -7.880000, "y": -10.100942}, {"x": -7.870000, "y": -9.724057}, {
		"x": -7.860000,
		"y": -5.722992
	}, {"x": -7.850000, "y": -5.135082}, {"x": -7.840000, "y": -9.872333}, {
		"x": -7.830000,
		"y": -7.163344
	}, {"x": -7.820000, "y": -9.230664}, {"x": -7.810000, "y": -7.397149}, {
		"x": -7.800000,
		"y": -7.310588
	}, {"x": -7.790000, "y": -9.620354}, {"x": -7.780000, "y": -6.301957}, {
		"x": -7.770000,
		"y": -7.982450
	}, {"x": -7.760000, "y": -7.450044}, {"x": -7.750000, "y": -10.198594}, {
		"x": -7.740000,
		"y": -7.495622
	}, {"x": -7.730000, "y": -10.380142}, {"x": -7.720000, "y": -4.536389}, {
		"x": -7.710000,
		"y": -9.485454
	}, {"x": -7.700000, "y": -9.126708}, {"x": -7.690000, "y": -8.528006}, {
		"x": -7.680000,
		"y": -5.785669
	}, {"x": -7.670000, "y": -9.679696}, {"x": -7.660000, "y": -6.381080}, {
		"x": -7.650000,
		"y": -5.388684
	}, {"x": -7.640000, "y": -4.429049}, {"x": -7.630000, "y": -5.251344}, {
		"x": -7.620000,
		"y": -5.306796
	}, {"x": -7.610000, "y": -8.326423}, {"x": -7.600000, "y": -10.112724}, {
		"x": -7.590000,
		"y": -5.076918
	}, {"x": -7.580000, "y": -6.596441}, {"x": -7.570000, "y": -9.423550}];

	var result = leastSquares(REQUIRED_SMOOTHING_DEGREE, data);

	var out = "[ ";
	for (var i = 0; i < result.length; i++) {
		out += ((i > 0 ? ", " : "") + result[i]);
	}
	out += " ]";
	console.log(out);
}
