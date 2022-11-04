package utils;

import org.junit.Test;
import utils.StaticUtil;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CloneTest {

    @Test
    public void testClone() {
        Map<Integer, String> toClone = Map.of(1, "Akeu",
                2, "Coucou",
                3, "Larigou");
        try {
            Object theClone = StaticUtil.deepCopy(toClone);
            assertTrue("Went well", ((Map<Integer, String>)theClone).size() == 3);
            assertTrue("First Element", ((Map<Integer, String>)theClone).get(1).equals("Akeu"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
