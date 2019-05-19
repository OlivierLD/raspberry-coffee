package gribprocessing.utils;

import calc.GeoPoint;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.NodeList;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

public class World {
	private static DOMParser parser = null;
	private static List<Polygon> lp = null;

	private static List<Polygon> getChartPolygon() {
		List<Polygon> listPolygon = new ArrayList<Polygon>();
		try {
			java.net.URL data = World.class.getResource("data.xml");
			if (parser == null)
				parser = new DOMParser();
			parser.parse(data);
			XMLDocument doc = parser.getDocument();
			NodeList nl = doc.selectNodes("//section");
			for (int i = 0; i < nl.getLength(); i++) {
				Polygon polygon = new Polygon();
				XMLElement section = (XMLElement) nl.item(i);
				NodeList nl2 = section.selectNodes("./point");
				for (int j = 0; j < nl2.getLength(); j++) {
					XMLElement pt = (XMLElement) nl2.item(j);
					String latValue = pt.getElementsByTagName("Lat").item(0).getFirstChild().getNodeValue();
					String lngValue = pt.getElementsByTagName("Lng").item(0).getFirstChild().getNodeValue();
					double l = Double.parseDouble(latValue);
					double g;
					for (g = Double.parseDouble(lngValue); g > 180D; g -= 180D) ;
					for (; g < -180D; g += 360D) ;
					polygon.addPoint((int) (g * 1000), (int) (l * 1000));
				}
				listPolygon.add(polygon);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return listPolygon;
	}

	public static boolean isInLand(GeoPoint gp) {
		if (lp == null)
			lp = getChartPolygon();
		boolean b = false;
		Point p = new Point((int) (gp.getG() * 1000), (int) (gp.getL() * 1000));
		for (Polygon poly : lp) {
			if (poly.contains(p)) {
				b = true;
				break;
			}
		}
		return b;
	}

	private final static int NB_ITERATION = 100;

	public static Polygon lineIntersectsPolygon(Point from, Point to, Polygon poly) {
		Polygon pg = null;

		int fromX = from.x;
		int fromY = from.y;
		int width = to.x - from.x;
		int height = to.y - from.y;

		for (int i = 0; i < NB_ITERATION; i++) {
			Point p = new Point(fromX + (int) (i * ((double) width / NB_ITERATION)), fromY + (int) (i * ((double) height / NB_ITERATION)));
			if (poly.contains(p.x, p.y)) {
//        GeoPoint gp = new GeoPoint((double)p.y / 1000D, (double)p.x / 1000D);
//        System.out.print(" (i=" + i + ", " + gp.toString() + ") ");
//        System.out.print(" fromX:" + fromX + ", fromY:" + fromY + ", w:" + width + ", h:" + height + " ");
//        System.out.print("FromPt:" + from + ", ToPt:" + to + " CurrPt:" + p + " ");
				pg = poly;
				break;
			}
		}
		return pg;
	}

	public static Polygon isRouteCrossingLand(GeoPoint from, GeoPoint to) {
		if (lp == null)
			lp = getChartPolygon();
		Polygon pg = null;

		Point pFrom = new Point((int) (from.getG() * 1000), (int) (from.getL() * 1000));
		Point pTo = new Point((int) (to.getG() * 1000), (int) (to.getL() * 1000));
		for (Polygon poly : lp) {
			Polygon inter = lineIntersectsPolygon(pFrom, pTo, poly);
			if (inter != null) {
				pg = inter;
				break;
			}
		}
		return pg;
	}

	public static void main(String[] args) {
		Polygon poly = new Polygon(new int[]{-10, -10, 10, 10},
				new int[]{-10, 10, 10, -10},
				4);
		Point from = new Point(-20, -20);
		Point to = new Point(20, 20);
		long before = System.currentTimeMillis();
		Polygon pg = lineIntersectsPolygon(from, to, poly);
		long after = System.currentTimeMillis();
		System.out.println("Intersection:" + Boolean.toString(pg != null));
		System.out.println("(" + Long.toString(after - before) + " ms)");

		from = new Point(-20, -20);
		to = new Point(-20, 20);

		before = System.currentTimeMillis();
		pg = lineIntersectsPolygon(from, to, poly);
		after = System.currentTimeMillis();
		System.out.println("Intersection:" + Boolean.toString(pg != null));
		System.out.println("(" + Long.toString(after - before) + " ms)");
	}
}
