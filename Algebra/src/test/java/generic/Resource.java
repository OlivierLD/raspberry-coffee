package generic;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class Resource {

    @Test
    public void testResource() {
        /*
         * See how to reach to a resource located under the resource folder.
         * Even from a test.
         */
        try {
            String name = this.getClass().getName();
            Class cls = Class.forName(name);
            ClassLoader classLoader = cls.getClassLoader();
            URL resource = classLoader.getResource("dummy.txt"); // At the root of the resources folder.
            if (resource != null) {
                String fName = resource.getFile();
                System.out.println(fName);
                File f = new File(fName);
                if (f.exists()) {
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    int nbLine = 0;
                    String line;
                    while ((line = br.readLine()) != null) {
                        nbLine++;
                        System.out.println(line);
                    }
                    br.close();
                    assertEquals(String.format("Expected 2 lines, got %d", nbLine), 2, nbLine);
                } else {
                    fail("File does not exist???");
                }
            } else {
                fail("Oops");
            }
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
