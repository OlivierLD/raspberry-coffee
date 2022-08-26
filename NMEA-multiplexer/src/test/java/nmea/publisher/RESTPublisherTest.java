package nmea.publisher;

import nmea.forwarders.RESTPublisher;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Run it like in
 * ../gradlew test --tests "nmea.publisher.RESTPublisherTest.testRESTEInk"
 */
public class RESTPublisherTest {

    private final static List<String> DATA_TO_SEND = List.of(
            "Ping", "Pong", "Paf",
            "Bing", "Boom", "Bang"
    );

    @Test
    public void testRESTEInk() {
        String wpl = "$GPWPL,3739.856,N,12222.812,W,OPMRNA*59";
        try {
            RESTPublisher restPublisher = new RESTPublisher();

            Properties props = new Properties();
            props.put("server.name", "localhost"); // That one must be up and running for this main to work.
            props.put("server.port", "8080");
//			props.put("rest.resource", "/rest/endpoint?qs=prm");
            props.put("rest.resource", "/eink2_13/display");
            props.put("rest.verb", "POST");
            props.put("http.headers", "Content-Type:plain/text");
            restPublisher.setProperties(props);

            for (int i = 0; i < 10; i++) {
                System.out.println(DATA_TO_SEND.get(i % DATA_TO_SEND.size()));
                try {
                    wpl = DATA_TO_SEND.get(i % DATA_TO_SEND.size()); // Comment that one if needed.
                    restPublisher.write(wpl.getBytes());
                } catch (Exception ex) {
                    System.err.println(ex.getLocalizedMessage());
                }
                try {
                    Thread.sleep(1_000L);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Oops");
        }
        assertTrue("Argh!", true);
    }
}
