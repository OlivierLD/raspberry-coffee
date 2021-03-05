package misc;

import matrix.PolynomialUtil;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class Algebra {

    @Test
    public void derivative() {
        double[] derivative = PolynomialUtil.derivative(new double[]{1, 2, 3});
        assertTrue("Bad luck", derivative.length == 2);
        assertTrue("Bad first derivative coeff", derivative[0] == 2d);
        assertTrue("Bad second derivative coeff", derivative[1] == 2d);
    }

    @Test
    public void dearIsaac() {
        double[] coeffs = new double[]{1, 2, 3, -4};
        List<Double> roots = PolynomialUtil.getRoots(coeffs);
        double approximate = PolynomialUtil.approximate(0, coeffs);
        assertTrue("Bad number of roots.", roots.size() == 2);
        assertTrue("Not close enough", (roots.get(0) - approximate) <= PolynomialUtil.PRECISION );
    }

}
