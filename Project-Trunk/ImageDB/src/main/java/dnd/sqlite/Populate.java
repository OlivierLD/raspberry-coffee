package dnd.sqlite;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Populate {
//    public static void main(String... args)
//            throws Exception {
//        Connection conn = SQLUtil.getConnection("." + File.separator + "db", "IMAGES", "images", "images");
//
//        try {
//            insertNewImage(conn, "." + File.separator + "img" + File.separator + "PA080038.JPG", new String[] {"Corine", "Olivier", "en mer", "1ere etape"});
//            insertNewImage(conn, "." + File.separator + "img" + File.separator + "PB180647.JPG", new String[] {"Olivier", "Banian", "Nuku-Hiva"});
//            insertNewImage(conn, "." + File.separator + "img" + File.separator + "PB300770.JPG", new String[] {"Corine", "Rangiroa", "Tiare"});
//
//            conn.close();
//            conn = null;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            if (conn != null) {
//                conn.close();
//            }
//        }
//    }

    private static void insertNewImage(Connection conn,
                                       String imgName,
                                       String[] tags) throws Exception {
        try {
            File fImg = new File(imgName);
            BufferedImage bufferedImage = ImageIO.read(fImg);
            int w = bufferedImage.getWidth();
            int h = bufferedImage.getHeight();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            byte[] data = baos.toByteArray();
            java.sql.Date d = new java.sql.Date(new java.util.Date().getTime());

            insertNewImage(conn, imgName, "jpg", w, h, d, data, tags);

            String stmt = "select count(*) from images";
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(stmt);
            while (rs.next()) {
                int nb = rs.getInt(1);
                System.out.println("Nb Image(s): " + nb);
            }
            rs.close();
            statement.close();

            stmt = "select name, width, height, created, image from images";
            statement = conn.createStatement();
            rs = statement.executeQuery(stmt);
            while (rs.next()) {
                String name = rs.getString(1);
                w = rs.getInt(2);
                h = rs.getInt(3);
                java.sql.Date cr = rs.getDate(4);
                byte[] imgData = rs.getBytes(5);
                System.out.println(name + ", " + w + ", " + h + ", " + cr.toString());

                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imgData));
                FileOutputStream fos = new FileOutputStream("img.jpg");
                ImageIO.write(image, "jpg", fos);
                fos.close();
            }
            rs.close();
            statement.close();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static void insertNewImage(Connection conn,
                                      String imgName,
                                      String type,
                                      int w,
                                      int h,
                                      java.sql.Date cr,
                                      byte[] data,
                                      String[] tags) throws Exception {
        boolean proceed = true;
        try {
            String checkExistenceStmt = "select count(*) from images where name = ?";
            PreparedStatement pStmt = conn.prepareStatement(checkExistenceStmt);
            pStmt.setString(1, imgName);
            ResultSet rs = pStmt.executeQuery();
            int nbRows = 0;
            while (rs.next()) {
                nbRows = rs.getInt(1);
            }

            if (nbRows != 0) {
                int resp = JOptionPane.showConfirmDialog(null, "Image [" + imgName + "] already exists.\nDo you want to update it?", "Update", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (resp == 0) {
                    String stmt = "update images set width = ?, height = ?, image = ?, created = ? where name = ?";
                    pStmt = conn.prepareStatement(stmt);
                    pStmt.setInt(1, w);
                    pStmt.setInt(2, h);
                    pStmt.setBytes(3, data);
                    pStmt.setDate(4, cr);
                    pStmt.setString(5, imgName);

                    pStmt.execute();
                    pStmt.close();
                    try {
                        conn.commit();
                    } catch (Exception ex) {
                        // Absorb
                    }
                } else if (resp == 1) {
                    proceed = false;
                } else if (resp == 2) {
                    throw new RuntimeException("Multiple load canceled");
                }
            } else {
                String stmt = "insert into images values (?, ?, ?, ?, ?, ?)";
                pStmt = conn.prepareStatement(stmt);
                pStmt.setString(1, imgName);
                pStmt.setString(2, type);
                pStmt.setInt(3, w);
                pStmt.setInt(4, h);
                pStmt.setBytes(5, data); // This is the BLOB
                pStmt.setDate(6, cr);

                pStmt.execute();
                pStmt.close();
                try {
                    conn.commit();
                } catch (Exception ex) {
                    // Absorb
                }
            }
            if (proceed) {
                for (int i = 0; (tags != null) && (i < tags.length); i++) {
                    String stmt = "insert into tags values (?, ?, ?)";
                    pStmt = conn.prepareStatement(stmt);
                    pStmt.setString(1, imgName);
                    pStmt.setInt(2, i + 1);
                    pStmt.setString(3, tags[i]);
                    pStmt.execute();
                    pStmt.close();
                }
            }
            try {
                conn.commit();
            } catch (Exception ex) {
                // Absorb
            }
        } catch (Exception ex) {
            System.err.println("For " + imgName + ", type:" + type);
            throw ex;
        }
    }
}