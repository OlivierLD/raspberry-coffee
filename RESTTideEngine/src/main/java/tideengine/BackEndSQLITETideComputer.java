package tideengine;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackEndSQLITETideComputer {

	// TODO DB Path from System variable...
	public final static String DB_PATH = "sql/tides.db";

	private static boolean verbose = false;

	private static Connection conn = null;

	static void connect() {
		try {
			Class.forName("org.sqlite.JDBC");
			String dbURL = String.format("jdbc:sqlite:%s", DB_PATH); // <- Make sure that one exists, or throw exception...
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
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	static void disconnect() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		} else {
			System.err.println("No connection to close...");
		}
	}

	static Constituents buildConstituents() throws Exception {
		Constituents constituents = null;
		if (conn != null) {

			constituents = new Constituents();
			Map<String, Constituents.ConstSpeed> constSpeedMap = constituents.getConstSpeedMap();

			String sqlStmt = "select t1.rank, t1.name, t2.coeffvalue from coeffdefs as t1 join speedconstituents as t2 on t1.name = t2.coeffname order by t1.rank";
			// "select t1.rank, t1.name, t2.coeffvalue, t3.year, t3.value as year_value from coeffdefs as t1 join speedconstituents as t2 on t1.name = t2.coeffname join equilibriums as t3 on t1.name = t3.coeffname order by t1.rank, t3.year";
			// "select t1.rank, t1.name, t2.coeffvalue, t3.year, t3.value as year_value from coeffdefs as t1 join speedconstituents as t2 on t1.name = t2.coeffname join nodefactors as t3 on t1.name = t3.coeffname order by t1.rank, t3.year";
			try {
				Statement statement = conn.createStatement();
				ResultSet rs = statement.executeQuery(sqlStmt);
				while (rs.next()) {
					int rank = rs.getInt(1);
					String name =  rs.getString(2);
					BigDecimal value = rs.getBigDecimal(3);
					// Create top map
					Constituents.ConstSpeed constSpeed = new Constituents.ConstSpeed(rank, name, value.doubleValue());
					constSpeedMap.put(name, constSpeed);
					if ("true".equals(System.getProperty("data.verbose", "false"))) {
						System.out.printf("Rank %d, coeff: %s, value: %f\n", rank, name, value);
					}
					// Nested queries here
					// Equilibriums
					String speedCoeffsStmt = "select t3.year, t3.value from equilibriums as t3 where t3.coeffname = ? order by t3.year";
					PreparedStatement preparedStatement1 = conn.prepareStatement(speedCoeffsStmt);
					preparedStatement1.setString(1, name);
					ResultSet speedCoeffsSet = preparedStatement1.executeQuery();
					while (speedCoeffsSet.next()) {
						int year = speedCoeffsSet.getInt(1);
						BigDecimal speedCoeffValue = speedCoeffsSet.getBigDecimal(2);
						// Populate equilibrium Map
						constSpeed.putEquilibrium(year, speedCoeffValue.doubleValue());
						if ("true".equals(System.getProperty("data.verbose", "false"))) {
							System.out.printf("\tSpeedCoeff -> Year %d, val: %f\n", year, speedCoeffValue);
						}
					}
					speedCoeffsSet.close();
					preparedStatement1.close();
					// nodefactors
					String nodeFactorStmt = "select t3.year, t3.value from nodefactors as t3 where t3.coeffname = ? order by t3.year";
					PreparedStatement preparedStatement2 = conn.prepareStatement(nodeFactorStmt);
					preparedStatement2.setString(1, name);
					ResultSet nodeFactorsSet = preparedStatement2.executeQuery();
					while (nodeFactorsSet.next()) {
						int year = nodeFactorsSet.getInt(1);
						BigDecimal nodeFactorValue = nodeFactorsSet.getBigDecimal(2);
						// Populate nodefactors Map
						constSpeed.putFactor(year, nodeFactorValue.doubleValue());
						if ("true".equals(System.getProperty("data.verbose", "false"))) {
							System.out.printf("\tNodeFactors -> Year %d, val: %f\n", year, nodeFactorValue);
						}
					}
					nodeFactorsSet.close();
					preparedStatement2.close();
				}
				rs.close();
				statement.close();
			} catch (SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		} else {
			System.err.println("No DB connection...");
			throw new RuntimeException("No DB connection...");
		}
		return constituents;
	}

	public static Stations getTideStations() throws Exception {
		return new Stations(getStationData());
	}

	public static Map<String, TideStation> getStationData() throws Exception {
		// TODO Flesh it out
		return null;
	}

	public static List<TideStation> getStationData(Stations stations) throws Exception {
		long before = System.currentTimeMillis();
		List<TideStation> stationData = new ArrayList<>();
		Set<String> keys = stations.getStations().keySet();
		for (String k : keys) {
			try {
				stationData.add(stations.getStations().get(k));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		long after = System.currentTimeMillis();
		if (verbose) {
			System.out.println("Finding all the stations took " + Long.toString(after - before) + " ms");
		}

		return stationData;
	}

	public static TideStation reloadOneStation(String stationName) throws Exception {
		return null;
	}

	public static void setVerbose(boolean verbose) {
		BackEndSQLITETideComputer.verbose = verbose;
	}
}
