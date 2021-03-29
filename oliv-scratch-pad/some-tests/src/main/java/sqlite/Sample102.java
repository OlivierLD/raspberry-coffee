package sqlite;

import java.io.File;
import java.sql.*;

/**
 * This code shows how to make JDBC connection to a SQLite database.<br/>
 * Requires the following dep in gradle:<br/>
 * <code>implementation group: 'org.xerial', name: 'sqlite-jdbc', version: '3.34.0'</code>
 * <br/>
 * Requires a table to exist:
 * <pre>
 * $ sqlite3 oliv.db
 * sqlite> CREATE TABLE OLIV_TABLE(id INTEGER PRIMARY KEY AUTOINCREMENT, data VARCHAR2, date DATETIME);
 * sqlite> INSERT INTO oliv_table (data, date) VALUES ("Hi there", datetime("now"));
 * sqlite> select * from oliv_table;
 * 1|Hi there|2021-03-29 01:51:51
 * </pre>
 */
public class Sample102 {

    public static void main(String... args) {
        String dbName = "oliv.db";
        System.out.printf("Running from %s%n", System.getProperty("user.dir"));
        File dbFile = new File(dbName);
        if (!dbFile.exists()) {
            System.out.printf("%s not found in %s... exiting%n", System.getProperty("user.dir"), dbName);
            System.exit(1);
        }
        String dbFullPath = dbFile.getAbsolutePath();
        try {
            Class.forName("org.sqlite.JDBC");
            String dbURL = String.format("jdbc:sqlite:%s", dbFullPath); // <- Make sure that one exists (see above)...
            Connection conn = DriverManager.getConnection(dbURL);
            if (conn != null) {
                System.out.println("Connected to the database");
                DatabaseMetaData dm = conn.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());

                Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery("select * from oliv_table");
                System.out.println("From the DB:");
                while (rs.next()) {
                    System.out.println("index = " + rs.getInt("id"));
                    System.out.println("data = " + rs.getString("data"));
                    System.out.println("date = " + rs.getString("date")); // Yes. getString.
                }
                rs.close();
                statement.close();

                statement = conn.createStatement();
                statement.executeUpdate("insert into oliv_table (data, date) VALUES (\"Hello world\", datetime(\"now\"))");
                statement.close();
                // conn.commit(); // Required if DB is NOT in auto-commit mode.

                conn.close();
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

