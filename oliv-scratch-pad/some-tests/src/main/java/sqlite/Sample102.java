package sqlite;

import java.sql.*;

/**
 * This program demonstrates making JDBC connection to a SQLite database.
 * Requires the following dep in gradle:
 * implementation group: 'org.xerial', name: 'sqlite-jdbc', version: '3.34.0'
 * Requires a table to exist:
 * ```
 * sqlite> CREATE TABLE OLIV_TABLE(id INTEGER PRIMARY KEY AUTOINCREMENT, data VARCHAR2, date DATETIME);
 * sqlite> INSERT INTO oliv_table (data, date) VALUES ("Hi there", datetime("now"));
 * sqlite> select * from oliv_table;
 * 1|Hi there|2021-03-29 01:51:51
 * sqlite> .quit
 * ```
 */
public class Sample102 {

    public static void main(String... args) {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbURL = "jdbc:sqlite:/home/pi/sqlite/oliv.db"; // <- Make sure that one exists...
            Connection conn = DriverManager.getConnection(dbURL);
            if (conn != null) {
                System.out.println("Connected to the database");
                DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());

                Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery("select * from oliv_table");
                System.out.println("From the DB:");
                while (rs.next()) {
                    System.out.println("data = " + rs.getString("data"));
                }

                conn.close();
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

