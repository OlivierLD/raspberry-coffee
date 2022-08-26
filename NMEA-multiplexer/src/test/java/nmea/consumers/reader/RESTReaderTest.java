package nmea.consumers.reader;

import nmea.consumers.client.RESTClient;
import org.junit.Test;
import utils.TimeUtil;

/**
 * Run it like in
 * ../gradlew test --tests "nmea.consumers.reader.RESTReaderTest.RESTReaderTest"
 */
public class RESTReaderTest {

    @Test
    public void RESTReader() {

        String serverNameOrIP = "localhost"; // ""192.168.1.102";

        RESTClient nmeaClient = new RESTClient();

        Runtime.getRuntime().addShutdownHook(new Thread("RESTClient shutdown hook") {
            public void run() {
                System.out.println("Shutting down nicely.");
                nmeaClient.stopDataRead();
            }
        });
        nmeaClient.initClient();
        nmeaClient.setReader(new RESTReader("RESTReader", nmeaClient.getListeners(),
                "http",
                serverNameOrIP,
                8_080,
                "/oplist/",
                ""));
        nmeaClient.startWorking();

        TimeUtil.delay(10_000L);
        nmeaClient.stopDataRead();
    }

}
