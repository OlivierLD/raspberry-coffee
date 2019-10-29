package implementation.perpetualalmanac;

import implementation.almanac.AlmanacComputer;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Publisher {
	private static DOMParser parser = new DOMParser();

	public static void generate(String fileName, int from, int to) throws Exception {
		PrintStream out = new PrintStream(new FileOutputStream(fileName));
		long before = System.currentTimeMillis();
		out.println("<perpetual-almanac>");
		for (int y = from; y <= to; y++) {
			System.out.println("Year " + y + "...");
			out.println("  <year value='" + Integer.toString(y) + "'>");
			for (int m = 1; m <= 12; m++) {
				out.println("    <month value='" + Integer.toString(m) + "'>");
				int nbd = AlmanacComputer.getNbDays(y, m);
				for (int d = 1; d <= nbd; d++) {
					out.println("      <day value='" + Integer.toString(d) + "'>");
					for (int h = 0; h <= 24; h++) {
						double[] data = Core.compute(y, m, d, h, 0, 0);
						String tag = "        <data hours='" + Integer.toString(h) + "' " +
								"minutes='" + Integer.toString(0) + "' " +
								"seconds='" + Integer.toString(0) + "'>";
						out.println(tag);
						out.println("          <sun-dec>" + Double.toString(data[0]) + "</sun-dec>");
						out.println("          <sun-gha>" + Double.toString(data[1]) + "</sun-gha>");
						out.println("          <aries-gha>" + Double.toString(data[2]) + "</aries-gha>");
						out.println("          <eot>" + Double.toString(data[3]) + "</eot>");
						out.println("          <sun-sd>" + Double.toString(data[4]) + "</sun-sd>");
						out.println("          <sun-hp>" + Double.toString(data[5]) + "</sun-hp>");
						out.println("        </data>");
					}
					out.println("      </day>");
				}
				out.println("    </month>");
			}
			out.println("  </year>");
		}
		out.println("</perpetual-almanac>");
		out.close();
		long after = System.currentTimeMillis();
		System.out.println("Computed is " + Long.toString(after - before) + " ms.");
	}

	public static void format(String in, String out) throws Exception {
		XMLDocument doc = null;
		long before = System.currentTimeMillis();
		synchronized (parser) {
			parser.parse(new File(in).toURI().toURL());
			doc = parser.getDocument();
		}
		long after = System.currentTimeMillis();
		System.out.println("Parsed in " + Long.toString(after - before) + " ms.");
		NodeList nl = doc.selectNodes("/perpetual-almanac//data");
		System.out.println("Found " + nl.getLength() + " nodes.");
	}

	public static void main(String... args) throws Exception {
		int from = Integer.parseInt(args[0]);
		int to = Integer.parseInt(args[1]);
		if (from < 1_900 || to > 2_100) {
			throw new RuntimeException("Only between 1900 and 2100");
		}
		if (from > to) {
			throw new RuntimeException("Bad chronology");
		}
		generate(args[2], from, to);
//  format("out.txt", "out.pdf");
	}
}
