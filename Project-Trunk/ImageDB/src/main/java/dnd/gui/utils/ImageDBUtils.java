package dnd.gui.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class ImageDBUtils {
	private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MMM-yyyy");
	public static final int OR_CONNECTOR = 1;
	public static final int AND_CONNECTOR = 2;

	public static int getNbRecTotal() {
		return nbRecTotal;
	}

	private static int nbRecTotal = -1;

	public static ArrayList<ImageDefinition> populateImageList(Connection conn) {
		return populateImageList(conn, (String) null);
	}

	public static ArrayList<ImageDefinition> populateImageList(Connection conn, int[] sort) {
		return populateImageList(conn, (String) null, 1, sort);
	}

	public static ArrayList<ImageDefinition> populateImageList(Connection conn, String filter) {
		return populateImageList(conn, filter, 1);
	}

	public static ArrayList<ImageDefinition> populateImageList(Connection conn, String filter, int logicalConnector) {
		return populateImageList(conn, filter, logicalConnector, null);
	}

	public static ArrayList<ImageDefinition> populateImageList(Connection conn, String filter, int logicalConnector, int[] sort) {
		ArrayList<ImageDefinition> al = new ArrayList();
		String imageStmt = "select distinct i.name, i.imagetype, i.width, i.height, i.created from images i";
		String tagStmt = "select label from tags where imgname = ? order by rnk";

		String connector = logicalConnector == ImageDBUtils.OR_CONNECTOR ? "or" : "and";

		if (filter != null) {
			if (filter.trim().length() > 0) {
				imageStmt = imageStmt + " where (";
				String[] fa = filter.trim().split(",");
				for (int i = 0; i < fa.length; i++) {
					imageStmt =
							imageStmt + (i > 0 ? " " + connector + " " : "") + "(exists (select t.label from tags t where t.imgname = i.name and upper(t.label) like upper('%"
									+ escapeQuote(fa[i].trim()) + "%')) " + "or" + " upper(i.name) like upper('%" + escapeQuote(fa[i].trim()) + "%') " + "or" + " upper(i.imagetype) like upper('%"
									+ escapeQuote(fa[i].trim()) + "%'))";
				}
				imageStmt = imageStmt + ")";
			}
		}
		if (sort != null) {
			String sortClause = "";
			for (int i = 0; i < sort.length; i++) {
				if (sort[i] != 0) {
					sortClause = sortClause + (sortClause.trim().length() > 0 ? ", " : "") + Integer.toString(i + 1);
					if (sort[i] == -1) {
						sortClause = sortClause + " desc";
					}
				}
			}
			if (sortClause.trim().length() > 0) {
				imageStmt = imageStmt + " order by " + sortClause;
			}
		}
		if (System.getProperty("verbose", "false").equals("true")) {
			System.out.println("Executing [" + imageStmt + "]");
		}
		int found = 0;
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(imageStmt);
			while (rs.next()) {
				found++;
				String name = rs.getString(1);
				String type = rs.getString(2);
				int w = rs.getInt(3);
				int h = rs.getInt(4);
				java.sql.Date cr = rs.getDate(5);
				String tags = "";
				PreparedStatement pStmt = conn.prepareStatement(tagStmt);
				pStmt.setString(1, name);
				ResultSet tagRS = pStmt.executeQuery();
				while (tagRS.next()) {
					String tag = tagRS.getString(1);
					tags = tags + (tags.trim().length() > 0 ? ", " : "") + tag;
				}
				tagRS.close();
				pStmt.close();
				ImageDefinition id = new ImageDefinition(name, type, w, h, SDF.format(new java.util.Date(cr.getTime())), tags);
				al.add(id);
			}
			rs.close();
			statement.close();
			if (nbRecTotal == -1) {
				nbRecTotal = found;
			}
			if (System.getProperty("verbose", "false").equals("true")) System.out.println("Found " + found + " row(s).");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return al;
	}

	public static String escapeQuote(String s)
	{
		return s.replace("'", "''");
	}

	public static ArrayList<ImageDefinition> populateUntaggedImageList(Connection conn) {
		ArrayList<ImageDefinition> al = new ArrayList();
		String imageStmt = "select distinct i.name, i.imagetype, i.width, i.height, i.created from images i where not exists (select * from tags t where t.imgname = i.name)";
		int found = 0;
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(imageStmt);
			while (rs.next()) {
				found++;
				String name = rs.getString(1);
				String type = rs.getString(2);
				int w = rs.getInt(3);
				int h = rs.getInt(4);
				java.sql.Date cr = rs.getDate(5);
				ImageDefinition id = new ImageDefinition(name, type, w, h, SDF.format(new java.util.Date(cr.getTime())), "");
				al.add(id);
			}
			rs.close();
			statement.close();
			if (System.getProperty("verbose", "false").equals("true")) System.out.println("Found " + found + " row(s).");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return al;
	}

	public static void deleteImage(Connection conn, String imgName) {
		String deleteStmt = "delete from images where name = ?";
		try {
			PreparedStatement pStmt = conn.prepareStatement(deleteStmt);
			pStmt.setString(1, imgName);
			pStmt.execute();
			pStmt.close();
			try {
				conn.commit();
			} catch (Exception ex) {
				// Absorb
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void extractImage(Connection conn, String imgName)
	{
		getImage(imgName, conn, imgName);
	}

	public static BufferedImage getImage(String imgName, Connection conn)
	{
		return getImage(imgName, conn, null);
	}

	public static BufferedImage getImage(String imgName, Connection conn, String fName) {
		BufferedImage image = null;
		try {
			String stmt = "select imagetype, image from images where name = ?";
			PreparedStatement statement = conn.prepareStatement(stmt);
			statement.setString(1, imgName);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String type = rs.getString(1);
				byte[] imgData = rs.getBytes(2);
				image = ImageIO.read(new ByteArrayInputStream(imgData));
				if (fName != null) {
					if (!fName.toLowerCase().endsWith(type)) {
						fName = fName + "." + type;
					}
					FileOutputStream fos = new FileOutputStream(fName);
					ImageIO.write(image, type, fos);
					fos.close();
				}
			}
			rs.close();
			statement.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	public static ArrayList<ImageDefinition> filterImageList(Connection conn, String filter, int logicalConnector) {
		return populateImageList(conn, filter, logicalConnector);
	}

	public static ArrayList<ImageDefinition> filterImageList(Connection conn, String filter, int logicalConnector, int[] sort) {
		return populateImageList(conn, filter, logicalConnector, sort);
	}

	public static ArrayList<ImageDefinition> untaggedImageList(Connection conn)
	{
		return populateUntaggedImageList(conn);
	}

	public static void updateTags(Connection conn, String pk, String[] tags) {
		String stmtDel = "delete from tags where imgname = ?";
		String stmtTags = "insert into tags values (?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(stmtDel);
			ps.setString(1, pk);
			ps.execute();
			ps.close();
			try {
				conn.commit();
			} catch (Exception ex) {
				// Absorb
			}

			for (int i = 0; i < tags.length; i++) {
				ps = conn.prepareStatement(stmtTags);
				ps.setString(1, pk);
				ps.setInt(2, i);
				ps.setString(3, tags[i].trim());
				ps.execute();
				ps.close();
			}
			try {
				conn.commit();
			} catch (Exception ex) {
				// Absorb
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
