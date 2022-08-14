package nmea.utils;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestDir {

    @Test
    public void testDir() {
        List<Float> deltaX = List.of( 20f, 30f, -30f, -20f, 0.001f );
        List<Float> deltaY = List.of( -30f, 100f, 200f, -200f, 0f );

        deltaX.forEach(x -> {
            deltaY.forEach(y -> {
                double dirObsolete = NMEAUtils.getDirObsolete(x, y);
                double newDir = NMEAUtils.getDir(x, y);
                assertEquals(String.format("Old and new are not the same, for x:%f y:%f", x, y), dirObsolete, newDir, 0.0001);
            });
        });
    }

    @Test(expected = RuntimeException.class)
    public void ambiguousGetDir() {
        double dir = NMEAUtils.getDir(0d, 0d);
    }

}
