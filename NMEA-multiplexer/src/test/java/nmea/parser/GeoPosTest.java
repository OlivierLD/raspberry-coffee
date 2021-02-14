package nmea.parser;

public class GeoPosTest {
    /**
     * Just for tests
     * @param args not used.
     */
    public static void main(String... args) {

        double test = GeoPos.sexToDec("140", "15.162");

        double lat = GeoPos.sexToDec("24", "03.76");
        double lng = GeoPos.sexToDec("109", "59.50") * -1; // West
        System.out.println(String.format("Grid Square La Ventana: %s", new GeoPos(lat, lng).gridSquare()));

        lat = GeoPos.sexToDec("37", "46");
        lng = GeoPos.sexToDec("122", "31") * -1; // West
        System.out.println(String.format("Grid Square Ocean Beach (SF) : %s", new GeoPos(lat, lng).gridSquare()));

        System.out.println(String.format("toString: %s", new GeoPos(lat, lng).toString()));
        System.out.println(String.format("Updated : %s", new GeoPos(lat, lng).updateGridSquare()));
    }
}
