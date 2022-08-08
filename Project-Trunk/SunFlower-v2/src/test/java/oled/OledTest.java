package oled;

import http.HttpHeaders;
import http.client.HTTPClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sunflower.httpserver.SunFlowerServer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OledTest {

    private SunFlowerServer sunFlowerServer;
    private final static int HTTP_PORT = 1234;

    @Before
    public void setup() {
        System.setProperty("http.port", String.valueOf(HTTP_PORT));
        System.setProperty("with.ssd1306", "true");
        try {
            sunFlowerServer = new SunFlowerServer();
//            sunFlowerServer.startHttpServer();
            try {
                Thread.sleep(10_000); // Wait a bit.
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @After
    public void tearDown() {
        try {
            String response = HTTPClient.doGet(String.format("http://localhost:%d/exit", HTTP_PORT), null);
            System.out.println(String.format("Returned >> %s", response));
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
            System.out.println(String.format("Returned >> %s", returnedPayload));
            assertEquals("Oops", "OLEDTest", returnedPayload);
        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
        try {
            Thread.sleep(2_000); // Wait a bit.
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        if (true) {
            // Not necessary, now we have the lock.wait(5_000L); in the shutdown hook in the SunFlowerDriver.
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
