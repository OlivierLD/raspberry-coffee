package sun;

import astrorest.AstroServer;
import http.HttpHeaders;
import http.client.HTTPClient;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class TestSunPath {
    // Invoke the sun path generation from REST
    // A - Start the server
    // B - Request
    // C - Stop the server


    private AstroServer astroServer;
    private final static int HTTP_PORT = 1234;

    @Before
    public void setup() {
        try {
            astroServer = new AstroServer();
            astroServer.startHttpServer(HTTP_PORT);
            try {
                Thread.sleep(10_000);
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

    private static String url = String.format("http://localhost:%d/astro/sun-path-today", HTTP_PORT);
    // In Vannes
    private static String payload = "{ position: { latitude: 47.661667, longitude: -2.758167 }, step: 10, utcdate: \"2022-03-06T01:00:00.000Z\" }"; // Payload
    // { position: { latitude: 37.76661945, longitude: -122.5166988 }, step: 10, utcdate: "DURATION" } . POST /astro/sun-path-today

    @Test
    public void testSunPath() {

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, HttpHeaders.APPLICATION_JSON);

        try {
            String returnedPayload = HTTPClient.doPost(url, headers, payload).getPayload();
            System.out.println(String.format("Returned >> %s", returnedPayload));
        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stop requested");
        }, "Shutdown Hook"));
    }

}
