package misc;

import matrix.PolynomialUtil;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class AlgebraTest {

    @Test
    public void derivative() {
        double[] derivative = PolynomialUtil.derivative(new double[]{1, 2, 3});
        assertEquals("Bad luck", 2, derivative.length);
        assertEquals("Bad first derivative coeff", 2d, derivative[0]);
        assertEquals("Bad second derivative coeff", 2d, derivative[1]);
    }

    @Test
    public void dearIsaac() {
        double[] coeffs = new double[]{1, 2, 3, -4};
        List<Double> roots = PolynomialUtil.getRoots(coeffs);
        double approximate = PolynomialUtil.approximate(0, coeffs);
        assertEquals("Bad number of roots.", 2, roots.size());
        assertTrue("Not close enough", (roots.get(0) - approximate) <= PolynomialUtil.PRECISION );
    }

}
