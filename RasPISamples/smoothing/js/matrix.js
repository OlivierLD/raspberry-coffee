
function SquareMatrix(dim) {
    var dimension = dim;
    var matrixElements = [];
    for (var row=0; row<dim; row++) {
        var line = [];
        for (var col=0; col<dim; col++) {
            line.push(0);
        }
        matrixElements.push(line);
    }

    this.getDimension = function() {
        return (dimension);
    };

    this.setElementAt = function(row, col, val) {
        matrixElements[row][col] = val;
    };

    this.getElementAt = function(row, col) {
        return matrixElements[row][col];
    };

    this.getMatrixElements = function() {
        return this.matrixElements;
    };

    this.setMatrixElements = function(me) {
        this.matrixElements = me;
    };
};

var minor = function(m, row, col) {
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

var comatrix = function(m) {
    var co = new SquareMatrix(m.getDimension());
    for (var r = 0; r < m.getDimension(); r++) {
        for (var c = 0; c < m.getDimension(); c++) {
            co.setElementAt(r, c, determinant(minor(m, r, c)) * Math.pow((-1), (r + c + 2)));  // r+c+2 = (r+1) + (c+1)...
        }
    }
    return co;
};

var transposed = function(m) {
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

var multiply = function(m, n) {
    var res = new SquareMatrix(m.getDimension());
    var r, c;
    for (r = 0; r < m.getDimension(); r++) {
        for (c = 0; c < m.getDimension(); c++) {
            res.setElementAt(r, c, m.getElementAt(r, c) * n);
        }
    }
    return res;
};

var determinant = function(m) {
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

var invert = function(m) {
//  return Transposed(Multiply(Comatrix(m), (1.0/Determin(m))));
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
var solveSystem = function(m, c) {
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
}

var printSystem = function(squareMatrix, constants) {
    var unknowns = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    var dimension = squareMatrix.getDimension();
    for (var row=0; row<dimension; row++) {
        var line = "";
        for (var col=0; col<dimension; col++) {
            line += ((line.trim().length > 0 ? " + " : "") + squareMatrix.getElementAt(row, col) + " x " + unknowns.charAt(col));
        }
        line += (" = " + constants[row]);
        console.log(line);
    }
}

/**
 * An example
 */
var squareMatrix = new SquareMatrix(3);

/*
 Resolution of:
 12x    +  13y +    14z = 234
 1.345x - 654y + 0.001z = 98.87
 23.09x + 5.3y - 12.34z = 9.876
 */
squareMatrix.setElementAt(0, 0, 12);
squareMatrix.setElementAt(0, 1, 13);
squareMatrix.setElementAt(0, 2, 14);

squareMatrix.setElementAt(1, 0, 1.345);
squareMatrix.setElementAt(1, 1, -654);
squareMatrix.setElementAt(1, 2, 0.001);

squareMatrix.setElementAt(2, 0, 23.09);
squareMatrix.setElementAt(2, 1, 5.3);
squareMatrix.setElementAt(2, 2, -12.34);

var constants = [234, 98.87, 9.876];

console.log("Solving:");
printSystem(squareMatrix, constants);

var result = solveSystem(squareMatrix, constants);

console.log("x = %d", result[0]);
console.log("y = %d", result[1]);
console.log("z = %d", result[2]);
console.log();
// Proof:
var X = (squareMatrix.getElementAt(0, 0) * result[0]) + (squareMatrix.getElementAt(0, 1) * result[1]) + (squareMatrix.getElementAt(0, 2) * result[2]);
console.log("Proof X: %d", X);
var Y = (squareMatrix.getElementAt(1, 0) * result[0]) + (squareMatrix.getElementAt(1, 1) * result[1]) + (squareMatrix.getElementAt(1, 2) * result[2]);
console.log("Proof Y: %d", Y);
var Z = (squareMatrix.getElementAt(2, 0) * result[0]) + (squareMatrix.getElementAt(2, 1) * result[1]) + (squareMatrix.getElementAt(2, 2) * result[2]);
console.log("Proof Z: %d", Z);

