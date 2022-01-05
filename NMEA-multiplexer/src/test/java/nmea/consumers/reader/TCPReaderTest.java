package nmea.consumers.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TCPReaderTest {
    /**
     * For tests. Can be used as a Canvas for a TCP client.
     * @param args Unused
     */
    public static void main(String... args) {
		/*
		-Dtcp.data.verbose=true
		-Dtcp.proxyHost=www-proxy.us.oracle.com
		-Dtcp.proxyPort=80
		-Dhttp.proxyHost=www-proxy.us.oracle.com
		-Dhttp.proxyPort=80
		-DsocksProxyHost=www-proxy.us.oracle.com
		-DsocksProxyPort=80

		-Dtcp.host="ais.exploratorium.edu"
		-Dtcp.port=80

		-Dtcp.host="sinagot.net"
		-Dtcp.port=2947
		 */

        String host = // "192.168.42.10";
                System.getProperty("tcp.host", "ais.exploratorium.edu");
        int port = Integer.parseInt(System.getProperty("tcp.port", "80")); // 7001; // 2947

        // TODO Also try sinagot.net:2947, as GPSd

        System.out.println(String.format("TCP Client for %s:%d", host, port));

        try {
            List<NMEAListener> ll = new ArrayList<>();
            NMEAListener nl = new NMEAListener() {
                @Override
                public void dataRead(NMEAEvent nmeaEvent) {
                    System.out.println(nmeaEvent.getContent()); // TODO Send to a GUI?
                }
            };
            ll.add(nl);

            boolean keepTrying = true;
            while (keepTrying) {
                TCPReader ctcpr = new TCPReader(ll, host, port);
                System.out.println(new Date().toString() + ": New " + ctcpr.getClass().getName() + " created.");

                try {
                    ctcpr.startReader();
                } catch (Exception ex) {
                    System.err.println("TCP Reader:" + ex.getMessage());
                    ctcpr.closeReader();
                    long howMuch = 1_000L;
                    System.out.println("Will try to reconnect in " + Long.toString(howMuch) + "ms.");
                    try {
                        Thread.sleep(howMuch);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
