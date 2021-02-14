package context;

public class NMEADataCacheTest {
    // For tests
    public static void main(String... args) {
        System.setProperty("nmea.cache.verbose", "true");
        System.setProperty("put.ais.in.cache", "false");
        NMEADataCache cache = new NMEADataCache();
        try {
            cache.parseAndFeed("$IIRMC,224044,A,0909.226,S,14015.162,W,06.7,222,211110,10,E,A*05");
            // Here, look at the cache
            System.out.println("Cache was fed.");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println("Done.");
    }
}
