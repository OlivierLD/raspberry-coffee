package utils.migration;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class PopulateFromFileSystem {

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

    private static Connection reopenConnection(Connection conn, String dbFullPath) throws Exception {
        if (conn != null) {
            conn.close();
        }
        String dbURL = String.format("jdbc:sqlite:%s", dbFullPath); // <- Make sure that one exists (see above)...
        conn = DriverManager.getConnection(dbURL);
        return conn;
    }

    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private final static String MIGRATION_DATA_PREFIX = "--migration-data-folder:";
    private final static String CSV_FILE_PREFIX = "--csv-file:";
    private final static String DB_FOLDER_PREFIX = "--db-folder:";
    private final static String DB_NAME_PREFIX = "--db-name:";
    /**
     * Populate the image DB
     * We assume that sql/image_db.sql has been run in sql/images.db
     * @param args --migration-data-folder:dump --csv-file:dump.csv --db-folder:sql --db-name:images.db
     */
    public static void main(String... args) throws Exception {

        AtomicReference<String> dataFolder = new AtomicReference<>("dump");
        AtomicReference<String> csvFile = new AtomicReference<>("dump.csv");
        AtomicReference<String> dbFolder = new AtomicReference<>("sql");
        AtomicReference<String> dbName = new AtomicReference<>("images.db");

        Arrays.asList(args).stream().forEach(arg -> {
            if (arg.startsWith(MIGRATION_DATA_PREFIX)) {
                dataFolder.set(arg.substring(MIGRATION_DATA_PREFIX.length()));
            } else if (arg.startsWith(CSV_FILE_PREFIX)) {
                csvFile.set(arg.substring(CSV_FILE_PREFIX.length()));
            } else if (arg.startsWith(DB_FOLDER_PREFIX)) {
                dbFolder.set(arg.substring(DB_FOLDER_PREFIX.length()));
            } else if (arg.startsWith(DB_NAME_PREFIX)) {
                dbName.set(arg.substring(DB_NAME_PREFIX.length()));
            }
        });

        File imageFolder = new File(dataFolder.get());
        if (!imageFolder.exists()) {
            throw new RuntimeException(String.format("%s not found.", imageFolder));
        }
        BufferedReader csv = new BufferedReader(new FileReader(String.format("%s%s%s", dataFolder.get(), File.separator, csvFile.get())));

        // Open DB
        Connection conn = null;
        File dbFile = new File(String.format("%s%s%s", dbFolder.get(), File.separator, dbName.get()));
        if (!dbFile.exists()) {
            System.out.printf("%s not found in %s... exiting%n", dbName.get(), dbFolder.get());
            System.exit(1);
        }
        String dbFullPath = dbFile.getAbsolutePath();
        try {
            Class.forName("org.sqlite.JDBC");
            String dbURL = String.format("jdbc:sqlite:%s", dbFullPath); // <- Make sure that one exists (see above)...
            conn = DriverManager.getConnection(dbURL);
            if (conn != null) {
                System.out.println("Connected to the database");
                DatabaseMetaData dm = conn.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());
            } else {
                System.out.println("Not connected?");
                System.exit(1);
            }
            // TODO Enable several connections. shared_cache, etc.
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        String line = "";
        boolean keepReading = true;
        String imageName = "";
        while (keepReading) {
            line = csv.readLine();
            if (line != null) {
                System.out.println(line);
                String[] data = line.split(";");
                if ("IMAGE".equals(data[0])) {
                    imageName = data[1];
                    String imageFileName = String.format("%s%s%s", dataFolder.get(), File.separator, imageName);
                    String SQLStatement = "insert into images (name, imagetype, width, height, image, created) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement statement = conn.prepareStatement(SQLStatement);
                    statement.setString(1, data[1]);
                    statement.setString(2, data[4]);
                    statement.setInt(3, Integer.parseInt(data[2]));
                    statement.setInt(4, Integer.parseInt(data[3]));
                    statement.setBytes(5, readFile(imageFileName));
                    Date sqlDate = new Date(SDF.parse(data[5]).getTime());
                    statement.setDate(6, sqlDate);
                    try {
                        statement.executeUpdate();
                    } catch (SQLException sqlEx) {
                        System.err.printf("Error executing [%s]\n", SQLStatement);
                        sqlEx.printStackTrace();
                        System.err.println("\t>> Moving on...");
                    }
                    statement.close();
//                    conn = reopenConnection(conn, dbFullPath);
                    // conn.commit(); // Required if DB is NOT in auto-commit mode.
                } else if ("TAG".equals(data[0])) {
                    String SQLStatement = "insert into tags (imgname, rnk, label) VALUES (?, ?, ?)";
                    PreparedStatement statement = conn.prepareStatement(SQLStatement);
                    statement.setString(1, imageName);
                    statement.setInt(2, Integer.parseInt(data[1]));
                    statement.setString(3, data[2]);
                    try {
                        statement.executeUpdate();
                    } catch (SQLException sqlEx) {
                        System.err.printf("Error executing [%s]\n", SQLStatement);
                        sqlEx.printStackTrace();
                        System.err.println("\t>> Moving on...");
                    }
                    statement.close();
//                    conn = reopenConnection(conn, dbFullPath);
                    // conn.commit();
                }
            } else {
                keepReading = false;
            }
        }
        csv.close();

        conn.close();
    }
}
