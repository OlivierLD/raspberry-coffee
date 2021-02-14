package nmea.parser;

public class PressureTest {

    public static void main(String... args) {
        Pressure p = new Pressure(1013.25);
        System.out.println(p.toString());
    }
}
