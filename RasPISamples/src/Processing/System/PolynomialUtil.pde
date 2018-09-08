import java.util.Arrays;

static class PolynomialUtil {

  private final static double precision = 1E-15;

  // Equation solving method
  private static List<Double> getRoots(double... coef) {
    List<Double> roots = new ArrayList<Double>();

    // Constant function
    if (coef.length == 0) {
      return roots;
    }

    // Linear function
    if (coef.length == 1) {
      roots.add(-coef[0]);
      return roots;
    }

    // One of its root is 0
    if (coef[coef.length - 1] == 0) {
      double[] newcoef = Arrays.copyOfRange(coef, 0, coef.length - 1);
      roots.addAll(getRoots(newcoef));
      roots.add(0.0d);
      return roots;
    }

    // Get derivative
    double[] newcoef = Arrays.copyOfRange(coef, 0, coef.length - 1);
    for (int i = 0; i < coef.length - 1; i++) {
      newcoef[i] *= (coef.length - 1 - i) / (double) coef.length;
    }

    // Get root of derivative
    List<Double> rootsA = getRoots(newcoef);
    /* rootsA.sort(new Comparator<Double>() {
      @Override
      public int compare(Double o1, Double o2) {
        return o1.compareTo(o2);
      }
    });*/

    // Get extreme points
    int n = rootsA.size();
    if (n == 0 && coef.length % 2 == 0) {
      return new ArrayList<Double>();
    } else if (n == 0 && coef.length % 2 == 1) {
      roots.add(approximate(0, coef));
      return roots;
    }

    // # There must be a unique root in an open interval
    // # if the signs of both ends are different
    // # by Intermediate Value Theorem
    // # There are n+1 open intervals,
    // # and each interval can have one or zero root.
    // Find root in each interval
    double[] x = new double[n];
    double[] fx = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = rootsA.get(i);
      fx[i] = fx(x[i], coef);
    }

    if (fx[n - 1] <= 0) {
      if (fx[n - 1] == 0) {
        roots.add(x[n - 1]);
      } else {
        roots.add(approximate(x[n - 1] + 1, coef));
      }
    }
    for (int i = n - 2; i >= 0; i--) {
      if (fx[i] * fx[i + 1] <= 0) {
        if (fx[i] == 0) {
          roots.add(x[i]);
        } else {
          roots.add(approximate((x[i] + x[i + 1]) / 2, coef));
        }
      }
    }
    if (fx[0] * (coef.length % 2 == 0 ? -1 : 1) >= 0) {
      if (fx[0] == 0) {
        roots.add(x[0]);
      } else {
        roots.add(approximate(x[0] - 1, coef));
      }
    }
    return roots;
  }

  // Returns function value
  private static double fx(double x, double... coef) {
    double res = 1;
    for (int i = 0; i < coef.length; i++) {
      res *= x;
      res += coef[i];
    }
    return res;
  }

  // Returns differential coefficient
  private static double fpx(double x, double... coef) {
    double res = coef.length;

    for (int i = 0; i < coef.length - 1; i++) {
      res *= x;
      res += coef[i] * (coef.length - 1 - i);
    }
    return res;
  }

  // Returns one of approximated root
  private static double approximate(double xn, double... coef) {
    try {
      double fx = fx(xn, coef);
      double fpx = fpx(xn, coef);

      double xn1 = fx / fpx;
      xn1 = xn - xn1;
      if (Math.abs(xn1 - xn) < precision) {
        return xn1;
      }
      return approximate(xn1, coef);
    } catch (StackOverflowError e) {
      return xn;
    }
  }

  /**
   *
   * @param coeff highest degree first
   * @return
   */
  public static List<Double> getPolynomRoots(double[] coeff) {
    double[] pCoeff = new double[coeff.length - 1];
    for (int i=1; i<coeff.length; i++) {
      pCoeff[i-1] = coeff[i] / coeff[0]; // TODO Check/assert if coeff[0] != 0
    }
    List<Double> roots = new ArrayList<Double>();

    List<Double> l = getRoots(pCoeff);
    while (l.size() != 0) {
      // There are roots!
      roots.addAll(l);

      // Check if there are more roots
      // Use all roots to factorize the given equation
      int ls = l.size();
      double[] tmp = new double[pCoeff.length - ls];
      for (double r : l) {
        pCoeff[0] += r;
        for (int i = 1; i < pCoeff.length; i++) {
          pCoeff[i] += pCoeff[i - 1] * r;
        }
      }
      // Use new coefficients for more roots
      for (int i = 0; i < tmp.length; i++) {
        tmp[i] = pCoeff[i];
      }
      pCoeff = tmp;
      l = getRoots(pCoeff);
    }
    return roots;
  }

  /**
   *
   * @param a highest degree first
   * @param b highest degree first
   * @return
   */
  public static double[] add(double[] a, double[] b) {
    int dim = Math.max(a.length, b.length);
    double[] sum = new double[dim];
    for (int i=0; i<dim; i++) {
      int aIdx = a.length - (dim - i);
      int bIdx = b.length - (dim - i);
      sum[i] = (aIdx < 0 ? 0 : a[aIdx]) + (bIdx < 0 ? 0 : b[bIdx]);
    }
    return sum;
  }

  /**
   *
   * @param a highest degree first
   * @param b highest degree first
   * @return
   */
  public static double[] multiply(double[] a, double[] b) {
    int productDim = a.length + b.length - 1;
    double[] product = new double[productDim];
    // init with 0
    for (int i=0; i<productDim; i++) {
      product[i] = 0d;
    }
    for (int x=0; x<a.length; x++) {
      for (int y=0; y<b.length; y++) {
        double prod = a[x] * b[y];
        int posInProd = productDim - (a.length - x + b.length - y) + 1;
        product[posInProd] += prod;
      }
    }
    return product;
  }

  /**
   * Derivative of a polynomial function
   * @param coeff
   * @return
   */
  public static double[] derivative(double[] coeff) {
    int dim = coeff.length - 1;
    double derCoeff[] = new double[dim];
    for (int i=0; i<dim; i++) {
      derCoeff[i] = (dim - i) * coeff[i];
    }
    return derCoeff;
  }

  /**
   * y = f(x)
   * @param curveCoeff highest degree first
   * @param x
   * @return
   */
  public static double f(double curveCoeff[], double x) {
    double y = 0;
    for (int degree=0; degree<curveCoeff.length; degree++) {
      y += (curveCoeff[degree] * Math.pow(x, curveCoeff.length - 1 - degree));
    }
    return y;
  }

  /**
   * Remove heading monomials with coeff 0
   * @param p
   * @return
   */
  public static double[] reduce(double[] p) {
    if (p[0] != 0) {
      return p;
    } else {
      int firstNonZero = 0;
      while (p[firstNonZero] == 0 && firstNonZero < (p.length - 1)) {
        firstNonZero++;
      }
      if (firstNonZero == p.length - 1) {
        throw new RuntimeException("All coefficients are 0");
      } else {
        int newDim = p.length - firstNonZero;
        double[] newPoly = new double[newDim];
        for (int i=0; i<newDim; i++) {
          newPoly[i] = p[i + firstNonZero];
        }
        return newPoly;
      }
    }
  }

  /**
   * Distance to each point of the curve y=f(x) is [(x - ptX)^2 + (f(x) - ptY)^2] ^ (1/2)
   * To get rid of the sqrt:
   * dist^2 = (x - ptX)^2 + (f(x) - ptY)^2
   * For the minimal distance, we are looking for roots of the derivative of the above.
   *
   * [f o g (x)]' = f(g(x))' = f'(g(x)) x g'(x)
   *
   * @param curve highest degree first
   * @param ptX
   * @param ptY
   * @return
   */
  public static double minDistanceToCurve(double curve[], double ptX, double ptY) {
    double dist = Double.MAX_VALUE;

    // distance pt = curve = distance between (x, f(x)) and (0, 3)
    // = (deltaX^2 + deltaY^2)^(1/2)
    //<=> distance^2 = (deltaX^2 + deltaY^2)
    // = (x - ptX)^2 + (f(x) - ptY)^2
    // Derivative: [2*(x-ptX)] + [2*(f(x) - ptY)*(f'(x))]
    //              |             |  |            |
    //              |             |  |            2-2
    //              |             |  2-1
    //              |             Part 2
    //              Part 1
    // Needed: polynomial addition, multiplication

    double[] part1 = multiply(new double[] { 1, -ptX }, new double[] { 2 });

    double[] part21 = add(curve, new double[] { -ptY });
    double[] part22 = derivative(curve);
    double[] part2 = multiply(multiply(part21, part22), new double[] { 2 });
    double[] full = add(part1, part2);
    List<Double> polynomRoots = getPolynomRoots(full);
    if (polynomRoots.size() == 0) {
      println("no root"); // TODO Throw exceptipon
    } else {
      for (double r : polynomRoots) {
        dist = Math.min(dist, dist(curve, r, ptX, ptY));
      }
    }
    return dist;
  }
  
  public static double dist(double[] coeff, double x, double ptX, double ptY) {
    double y = f(reduce(coeff), x);
    return Math.sqrt(Math.pow(x - ptX, 2) + Math.pow(y - ptY, 2));
  }

  public static String display(double[] p) {
    String display = "";
    for (int i=0; i<p.length; i++) {
      display += (String.format("%+f%s ", p[i], (i == (p.length - 1) ? "" : (i == (p.length - 2) ? " * x" : String.format(" * x^%d", (p.length - 1 - i))))));
    }
    return display;
  }
}
