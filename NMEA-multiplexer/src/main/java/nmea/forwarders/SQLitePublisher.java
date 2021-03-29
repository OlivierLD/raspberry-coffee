package nmea.forwarders;

import nmea.parser.StringParsers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * <b>For dynamic loading</b> (non standard, as an example)
 * --------------------------
 * Requires a file like sqlite.properties to provide the broker url.
 * Requires a table NMEA_DATA created like this:
 * CREATE TABLE NMEA_DATA(id INTEGER PRIMARY KEY AUTOINCREMENT, sentence_id VARCHAR2(3), data VARCHAR2, date DATETIME);
 *
 * Requires the following dep in gradle:<br/>
 * <code>implementation group: 'org.xerial', name: 'sqlite-jdbc', version: '3.34.0'</code>
 *
 * SQLite doc at https://sqlite.org/lang_select.html
 */
public class SQLitePublisher implements Forwarder {
	private Connection dbConnection = null;
	private String dbURL;

	private Properties props;

	public SQLitePublisher() throws Exception {
		super();
	}

	/*
	 * dbURL like jdbc:sqlite:/path/to/db.db
	 */
	private void initConnection() throws Exception {
		if (this.props == null) {
			throw new RuntimeException("Need props!");
		}
		String dbUrl = this.props.getProperty("db.url");
		if (dbUrl == null) {
			throw new RuntimeException("No db.url found in the props...");
		}
		this.dbURL = dbUrl;

		try {

			this.dbConnection = DriverManager.getConnection(dbURL);
			if ("true".equals(props.getProperty("verbose"))) {
				DatabaseMetaData dm = dbConnection.getMetaData();
				System.out.println("Driver name: " + dm.getDriverName());
				System.out.println("Driver version: " + dm.getDriverVersion());
				System.out.println("Product name: " + dm.getDatabaseProductName());
				System.out.println("Product version: " + dm.getDatabaseProductVersion());
			}

		} catch (Exception e) {
			throw e;
		}
	}

	public String getDbURL() {
		return this.dbURL;
	}

	@Override
	public void write(byte[] message) {

		if (this.dbConnection == null) {
			try {
				initConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		try {
			String mess = new String(message);
			if (!mess.isEmpty()) {
				String sentenceId = StringParsers.getSentenceID(mess);
				String SQLStatement = String.format(
						"INSERT INTO NMEA_DATA (sentence_id, data, date) VALUES (\"%s\", \"%s\", datetime(\"now\"))",
						sentenceId, mess);
				// TODO More verbose?
				Statement statement = this.dbConnection.createStatement();
				statement.executeUpdate(SQLStatement);
				statement.close();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			this.dbConnection.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static class SQLiteBean {
		private String cls;
		private String dbURL;
		private String type = "sqlite";

		public SQLiteBean(SQLitePublisher instance) {
			cls = instance.getClass().getName();
			dbURL = instance.dbURL;
		}

		public String getDbURL() {
			return dbURL;
		}
	}

	@Override
	public Object getBean() {
		return new SQLiteBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}
