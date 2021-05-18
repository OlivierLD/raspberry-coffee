package sqlite;

import java.io.*;
import java.sql.*;

/**
 * Good doc at https://www.sqlitetutorial.net/sqlite-java/jdbc-read-write-blob/
 * <p>
 * We use here a table created like:
 * CREATE TABLE PHOTOS (id INTEGER PRIMARY KEY AUTOINCREMENT, image_name VARCHAR2, description VARCHAR2, picture BLOB);
 * CREATE UNIQUE INDEX IMAGE_NAME_IDX ON PHOTOS (IMAGE_NAME);
 */
public class SampleBLOB {

    /**
     * Read the file and returns the byte array
     * @param file the file name.
     * @return the bytes of the file
     */
    private static byte[] readFile(String file) {
        ByteArrayOutputStream bos = null;
        try {
            File f = new File(file);
            FileInputStream fis = new FileInputStream(f);
            byte[] buffer = new byte[1_024];
            bos = new ByteArrayOutputStream();
            int len;
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        return bos != null ? bos.toByteArray() : null;
    }

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

                // Insert Image in DB
                if (true) {
                    String imageFileName = "./images/jconsole.png";
                    String SQLStatement = "insert into photos (description, image_name, picture) VALUES (?, ?, ?)";
                    PreparedStatement statement = conn.prepareStatement(SQLStatement);
                    statement.setString(1, "First image test");
                    statement.setString(2, imageFileName.substring(imageFileName.lastIndexOf("/") + 1));
                    statement.setBytes(3, readFile(imageFileName));
                    try {
                        statement.executeUpdate();
                        System.out.println("Picture inserted in BLOB");
                    } catch (SQLException sqlEx) {
                        System.err.printf("Error executing [%s]\n", SQLStatement);
                        sqlEx.printStackTrace();
                        System.err.println("\t>> Moving on...");
                    }
                    statement.close();
                    // conn.commit(); // Required if DB is NOT in auto-commit mode.
                }
                // Now let's query the image
                String selectSQL = "select description, image_name, picture from photos";  // No where clause here, this is just an example
                Statement selectStatement = conn.createStatement();
                ResultSet resultSet = selectStatement.executeQuery(selectSQL);
                int imageIndex = 0;
                while (resultSet.next()) {
                    System.out.printf("- Reading record #%d%n", ++imageIndex);
                    String dbFileName = resultSet.getString("image_name");
                    String description = resultSet.getString("description");
                    InputStream inputStream = resultSet.getBinaryStream("picture");

                    String extracted = "_from_DB_" + dbFileName;
                    FileOutputStream fileOutputStream = new FileOutputStream(extracted);
                    byte[] buffer = new byte[1_024];
                    while (inputStream.read(buffer) > 0) {
                        fileOutputStream.write(buffer);
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    System.out.printf("Finished with [%s], in %s%n", description, extracted);
                }
                resultSet.close();
                selectStatement.close();

                conn.close();
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("Loading the driver:");
            ex.printStackTrace();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
