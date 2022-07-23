package gribprocessing.utils;

import utils.StaticUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GRIBUtils {

	public final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_z");

	/**
	 * For SailMail requests
	 *
	 * @param gribRequest, like "GFS:65N,45S,130E,110W|2,2|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN"
	 * @return
	 */
	public static String generateGRIBRequest(String gribRequest) {
		String request = "";
		try {
			String inputString = gribRequest + " LeDiouris/6ce9Ci7t";

			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] buff = new byte[inputString.length()];
			for (int i = 0; i < inputString.length(); i++) {
				buff[i] = (byte) inputString.charAt(i);
			}
			digest.update(buff, 0, inputString.length());

			String s = "";
			byte[] md5encoded = digest.digest();
			//    System.out.println("Final len:" + md5encoded.length);
			for (int i = 0; i < md5encoded.length; i++) {
				char c = (char) md5encoded[i];
				String str = Integer.toString(c, 16);
				if (str.length() == 4) {
					str = str.substring(2, 4);
				} else if (str.length() == 1) {
					str = "0" + str;
				}
				//      System.out.print(str + " ");

				s += str;
			}
			//    System.out.println();
			//    System.out.println("Java MD5 [" + s + "]");
			//    System.out.println("10 first chars [" + s.substring(0, 10) + "]");

			//    System.out.println("Final Request: [http://saildocs.com/fetch?" + gribRequest + "&3=" + s.substring(0, 10) + "&u]" );
			request = "http://saildocs.com/fetch?" + gribRequest + "&3=" + s.substring(0, 10) + "&u";
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return request;
	}

	public static byte[] getGRIB(String urlString, String dir, String fileName, boolean verbose) throws Exception {
		byte[] content = null;
		String retFile = "";
		String outputdir = dir;
		String urlStr = urlString;
		if (urlStr.indexOf(";") > -1) {
			urlStr = urlStr.substring(0, urlStr.indexOf(";"));
			outputdir += (File.separator + urlString.substring(urlString.indexOf(";") + 1));
		}
		try {
			long before = System.currentTimeMillis();
			if (verbose) {
				System.out.println("...reading (2) " + urlStr);
			}
			String fName = fileName;
			if (fName == null) {
				fName = outputdir + File.separator + "GRIB" + SDF.format(new Date()) + ".grb";
			}
			try {
//      System.out.println(request);
				URL saildocs = new URL(urlString);
				URLConnection connection = saildocs.openConnection();
				connection.connect();
				//    DataInputStream dis = new DataInputStream(connection.getInputStream());
				InputStream dis = connection.getInputStream();

				long waiting = 0L;
				while (dis.available() == 0 && waiting < 30L) { // 30s Timeout...
					Thread.sleep(1000L);
					waiting += 1L;
				}

				final int BUFFER_SIZE = 65_536;
				byte aByte[] = new byte[BUFFER_SIZE];
				int nBytes;
				while ((nBytes = dis.read(aByte, 0, BUFFER_SIZE)) != -1) {
//        System.out.println("Read " + nBytes + " more bytes.");
					content = StaticUtil.appendByteArrays(content, aByte, nBytes);
				}
//			System.out.println("Read " + content.length + " bytes.");
				if (verbose) {
					System.out.println("Read " + NumberFormat.getInstance().format(content.length) + " bytes of GRIB data.");
				}
				dis.close();
				ByteArrayInputStream bais = new ByteArrayInputStream(content);
				dis = bais; // switch
			} catch (Exception e) {
				e.printStackTrace();
			}
			File f = new File(outputdir, fName);
			if (new File(outputdir).canWrite()) {
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(content);
				fos.close();
				long diff = System.currentTimeMillis() - before;
				retFile = f.getAbsolutePath();
				if (verbose) {
					System.out.println("New GRIB available " + retFile + " [" + Long.toString(diff) + " ms]");
				}
			} else
				throw new RuntimeException("Cannot write in " + outputdir);
		} catch (Exception e) {
			throw e;
		}
		return content;
	}

	public static void main(String... args) {
		String request = "GFS:65N,45S,130E,110W|2,2|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN";

		try {
			GRIBUtils.getGRIB(GRIBUtils.generateGRIBRequest(request), ".", "grib.grb", true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
