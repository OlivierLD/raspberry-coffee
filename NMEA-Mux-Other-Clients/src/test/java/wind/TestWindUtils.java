package wind;

import org.junit.Test;
import utils.WindUtils;

import static org.junit.Assert.assertEquals;

public class TestWindUtils {

    @Test
    public void testBeaufort() {
        double tws = 20d;
        final int beaufort = WindUtils.getBeaufort(tws);
        assertEquals("Wrong Beaufort value", 5, beaufort);
    }
}
