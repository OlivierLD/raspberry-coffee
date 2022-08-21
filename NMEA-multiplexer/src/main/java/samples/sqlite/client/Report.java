package samples.sqlite.client;

import nmea.parser.GeoPos;
import nmea.parser.RMC;
import nmea.parser.StringParsers;
import java.util.Date;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * An example.
 * Shows how to parse the NMEA data stored in SQLite.
 */
public class Report {
    /*
     * dbURL like jdbc:sqlite:/path/to/db.db
     */
    private final static String DEFAULT_DB_URL = "jdbc:sqlite:nmea.db";

    private Connection dbConnection = null;
    private String dbURL = DEFAULT_DB_URL;

    /**
     * All values in degrees
     * @return in degrees
     */
    private static double getDistance(double fromL, double fromG, double toL, double toG) {
        double cos = (Math.sin(Math.toRadians(fromL)) * Math.sin(Math.toRadians(toL))) +
                (Math.cos(Math.toRadians(fromL)) * Math.cos(Math.toRadians(toL)) * Math.cos(Math.toRadians(toG - fromG)));
        double inRadians = Math.acos(Math.min(cos, 1.0)); // Trick
        return Math.toDegrees(inRadians);
    }

    public void report() throws Exception {
        try {

            this.dbConnection = DriverManager.getConnection(dbURL);
            if (true || "true".equals(System.getProperty("verbose"))) {
                DatabaseMetaData dm = dbConnection.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());
            }

            String SQLStatement = "SELECT DATA FROM NMEA_DATA WHERE SENTENCE_ID = 'RMC'";

            GeoPos prevPos = null;
            double dist = 0d;
            Date firstDate = null;
            Date lastDate = null;

            Statement statement = this.dbConnection.createStatement();
            final ResultSet resultSet = statement.executeQuery(SQLStatement);
            while (resultSet.next()) {
                final String rmc = resultSet.getString(1);
//                System.out.println(rmc);
                final RMC parsedRMC = StringParsers.parseRMC(rmc);
                if (parsedRMC.isValid()) {
                    if (firstDate == null) {
                        firstDate = parsedRMC.getRmcDate();
                    }
                    lastDate = parsedRMC.getRmcDate();
                    final double sog = parsedRMC.getSog();
                    System.out.printf("SOG: %.02f kn\n", sog);
                    final double lat = parsedRMC.getGp().lat;
                    final double lng = parsedRMC.getGp().lng;
                    if (prevPos != null) {
                        double thisSegment = getDistance(prevPos.lat, prevPos.lng, lat, lng);
                        dist += (thisSegment * 60d);
                    }
                    prevPos = parsedRMC.getGp();
                }
            }
            System.out.printf("Total distance: %.02f nm, in %d sec\n", dist, (lastDate.getTime() - firstDate.getTime()) / 1_000);

            resultSet.close();
            statement.close();

        } catch (Exception e) {
            throw e;
        }

    }

    public static void main(String... args) throws Exception {
        new Report().report();
    }
}
