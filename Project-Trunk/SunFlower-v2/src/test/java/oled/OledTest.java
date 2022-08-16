package oled;

//import http.HttpHeaders;
import http.client.HTTPClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sunflower.main.SunFlowerServer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OledTest {

//    private SunFlowerServer sunFlowerServer; // now a local variable in the setup.
    private final static int HTTP_PORT = 1_234;
    private final static boolean FORCE_SUBSTITUTE_SHUTDOWN = false;

    @Before
    public void setup() {
        // Some system props, and start the server
        System.setProperty("http.port", String.valueOf(HTTP_PORT));
        System.setProperty("with.ssd1306", "true");
        try {
            // May cause a ClassNotFoundException on Linux. Weird.
         /* SunFlowerServer sunFlowerServer = */ new SunFlowerServer(); // Also starts the server
            try {
                Thread.sleep(10_000L); // Wait a bit.
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @After
    public void tearDown() {
        // Shut down the server started in setup.
        try {
            String response = HTTPClient.doGet(String.format("http://localhost:%d/exit", HTTP_PORT), null);
            System.out.printf("Returned >> %s\n", response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testOledScreen() {
        Map<String, String> headers = new HashMap<>();
//        headers.put(HttpHeaders.CONTENT_TYPE, HttpHeaders.APPLICATION_JSON);
        String url = String.format("http://localhost:%d/sf/test-oled?value=OLEDTest", HTTP_PORT);
        try {
            String returnedPayload = HTTPClient.doGet(url, headers);
            System.out.printf("Returned >> %s\n", returnedPayload);
            assertEquals("Oops", "OLEDTest", returnedPayload);
        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
        try {
            Thread.sleep(2_000L); // Wait a bit.
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        if (FORCE_SUBSTITUTE_SHUTDOWN) {
            // Not necessary, as now we have the lock.wait(5_000L); in the shutdown hook in the SunFlowerDriver.
            try {
                final HTTPClient.HTTPResponse httpResponse = HTTPClient.doPost(String.format("http://localhost:%d/sf/force-shutdown-substitute", HTTP_PORT), headers, null);
                assertEquals("Expected 200/OK", 200, httpResponse.getCode());
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
        }
        System.out.println("Test is completed.");
    }

}
