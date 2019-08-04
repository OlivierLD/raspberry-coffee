// Groovy System resolution.

def matrix = [
  [ 12, 13, 14 ],
  [1.345, -654, 0.001 ],
  [23.09, 5.3, -12.34 ]
]

def coeffs = [234, 98.87, 9.876]

def debug = false

def minor = { List<List<Double>> m, int row, int col ->
    def dim = m.size()
    def min = new ArrayList<List<Double>>(dim - 1)
    (dim - 1).times {
        min.add(new ArrayList<Double>(dim - 1))
    }

    for (int c=0; c<dim; c++) {
        if (c != col) {
            for (int r=0; r<dim; r++) {
                if (r != row) {
                    def minR = (r < row) ? r : (r - 1)
                    min.get(minR).add(m.get(r).get(c))
                }
            }
        }
    }
    return min
}

def printMatrix = { List<List<Double>> m, boolean withCR = true ->
    for (int r=0; r<m.size(); r++) {
        print("| ");
        for (int c=0; c<m.get(r).size(); c++) {
            print(" ${m.get(r).get(c)} ")
        }
        if (r == (m.size() - 1) && !withCR) {
            print(" |")
        } else {
            println(" |")
        }
    }
}

def determinant // For recursion. Define before, so you can use it inside.
determinant = { List<List<Double>> m, int level = 1 ->
    def value = 0.0;

    if (m.size() == 1) {
        value = m.get(0).get(0)
    } else {
        for (int C = 0; C < m.size(); C++) {
            if (debug) {
                println " >> Level ${level}, C ${C}"
            }
            def det = determinant(minor(m, 0, C), level + 1)
//            println "--- Adding det:${det}, C:${C}, matrix elem ${matrix.get(0).get(C)}, pow: ${Math.pow(-1.0, C + 1 + 1)}"
            value += (m.get(0).get(C) * det * Math.pow(-1.0, C + 1 + 1))
        }
    }
    if (debug) {
        println("Determinant of")
        printMatrix(m, false)
        println " is ${value}"
    }
    return value;
}

def multiply = { List<List<Double>> m, double n ->
    def dim = m.size()
    def res = new ArrayList<List<Double>>(dim)
    dim.times {
        res.add(new ArrayList<Double>(dim))
    }

    for (int r = 0; r < dim; r++) {
        for (int c = 0; c < dim; c++) {
            res.get(r).add(m.get(r).get(c) * n)
        }
    }
    return res;
}

def comatrix = { List<List<Double>> m ->
    def dim = m.size()
    def comat = new ArrayList<List<Double>>(dim)
    dim.times {
        comat.add(new ArrayList<Double>(dim))
    }
    for (int r = 0; r < dim; r++) {
        for (int c = 0; c < dim; c++) {
            comat.get(r).add(determinant(minor(m, r, c)) * Math.pow((-1), (r + c + 2)))
        }
    }
    return comat;
}

// Groovy has a transpose() built-in
def invert = { List<List<Double>> m ->
    return multiply(comatrix(m).transpose(), (1.0 / determinant(m)))
}

def solveSystem = { List<List<Double>> m, List<Double> cf ->
    def dim = cf.size()
    List<Double> result = new ArrayList<>(dim)
    def inv = invert(m)
    for (int r=0; r<dim; r++) {
        def cell = 0.0
        for (int c=0; c<dim; c++) {
            cell += inv.get(r).get(c) * cf.get(c)
        }
        result.add(cell)
    }
    return result
}

def printSystem = { List<List<Double>> m, List<Double> cf ->
    def unknowns = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    def dim = cf.size()
    for (int r=0; r<dim; r++) {
        for (int c=0; c<dim; c++) {
            print "${ c==0 ? "" : " + "}(${m.get(r).get(c)} x ${unknowns[c]}) "
        }
        println " = ${cf[r]}"
    }
}

println "Resolving"
printSystem(matrix, coeffs)
def result = solveSystem(matrix, coeffs)
println "A= ${result[0]}"
println "B= ${result[1]}"
println "C= ${result[2]}"


