package dnd.gui;

import dnd.gui.ctx.AppContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class MainGUI {

	private static Connection getConnection(String dbLocation) {
		Connection conn = null;
		File dbFile = new File(dbLocation);
		if (!dbFile.exists()) {
			System.out.printf("%s not found... exiting%n", dbLocation);
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
		return conn;
	}

	public MainGUI(String dbLoc) {
		String dbLocation = dbLoc == null ? System.getProperty("db.location", "sql" + File.separator + "images.db") : dbLoc;

		try {
			Connection conn = getConnection(dbLocation);
			AppContext.getInstance().setConn(conn);
		} catch (Exception ex) {
			System.err.println("From " + System.getProperty("user.dir"));
			ex.printStackTrace();
		}
		JFrame frame = new MainFrame();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				AppContext.getInstance().closeConnection();
				System.exit(0);
			}
		});
		frame.setVisible(true);
	}

	private final static String DB_LOCATION_PREFIX = "--db-location:";

	public static void main(String... args) {

		AtomicReference<String> dbLoc = new AtomicReference<>(null);
		Arrays.asList(args).stream().forEach(arg -> {
			if (arg.startsWith(DB_LOCATION_PREFIX)) {
				dbLoc.set(arg.substring(DB_LOCATION_PREFIX.length()));
			}
		});

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		new MainGUI(dbLoc.get());
	}
}
