package samples.rmi.client;

import context.NMEADataCache;
import nmea.forwarders.rmi.ServerInterface;
import nmea.forwarders.rmi.clientoperations.BoatPosition;
import nmea.forwarders.rmi.clientoperations.LastString;
import nmea.forwarders.rmi.clientoperations.NMEACache;
import nmea.forwarders.rmi.clientoperations.TrueWind;
import nmea.parser.GeoPos;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.NumberFormat;

/**
 * An example of the way to invoke the RMI server
 */
public class SampleRMIClient {

	private static String bindingName = "RMIServer";

	// For the MUX: olediouris-mbp/10.10.226.181, port 1099, name RMI-NMEA
	public static void main(String args[]) {
		String name;
		String port;

		if (args.length != 2) {
			System.out.println("Arguments: [RMI Server Name] [Port]");
			name = "olediouris-mbp"; // "10.10.226.181";
			port = "1099";
		} else {
			name = args[0];
			port = args[1];
		}

		bindingName = "RMI-NMEA";

		System.out.println("Looking up [" + bindingName + " on " + name + ":" + port + "]");
		try {
			long before = System.currentTimeMillis();
			Registry registry = LocateRegistry.getRegistry(name, new Integer(port)); // Server name, port
			Remote remote = registry.lookup(bindingName);
			System.out.println("Remote is a " + remote.getClass().getName());
			ServerInterface comp = (ServerInterface) registry.lookup(bindingName);   // RMI Name
			long after = System.currentTimeMillis();
			System.out.println(String.format("Lookup took %s ms.", NumberFormat.getInstance().format(after - before)));

			LastString task = new LastString();
			before = System.currentTimeMillis();
			String last = comp.executeTask(task);
			after = System.currentTimeMillis();
			System.out.println(String.format("LastString execution took %s ms.", NumberFormat.getInstance().format(after - before)));
			System.out.println(last);

			try { Thread.sleep(1000L); } catch (InterruptedException ie) {}

			NMEACache cacheTask = new NMEACache();
			before = System.currentTimeMillis();
			NMEADataCache cache = comp.executeTask(cacheTask);
			after = System.currentTimeMillis();
			System.out.println(String.format("NMEACache execution took %s ms.", NumberFormat.getInstance().format(after - before)));
			Object position = cache.get(NMEADataCache.POSITION);
			System.out.println("Position is a " + position.getClass().getName());
			if (position instanceof GeoPos) {
				System.out.println(String.format("Position is %s", ((GeoPos)position).toString()));
			}

			try { Thread.sleep(1000L); } catch (InterruptedException ie) {}

			BoatPosition boatPositionTask = new BoatPosition();
			before = System.currentTimeMillis();
			GeoPos boatGeoPos = comp.executeTask(boatPositionTask);
			after = System.currentTimeMillis();
			System.out.println(String.format("BoatPosition execution took %s ms.", NumberFormat.getInstance().format(after - before)));
			System.out.println(String.format("Position is %s", boatGeoPos.toString()));

			try { Thread.sleep(1000L); } catch (InterruptedException ie) {}

			TrueWind trueWind = new TrueWind();
			for (int i=0; i<50; i++) {
				before = System.currentTimeMillis();
				nmea.parser.TrueWind tw = comp.executeTask(trueWind);
				after = System.currentTimeMillis();
				System.out.println(String.format("TrueWind execution took %s ms.", NumberFormat.getInstance().format(after - before)));
				System.out.println(String.format("TW is %f knots, from %d", tw.speed, tw.angle));
				try { Thread.sleep(1000L); } catch (InterruptedException ie) {}
			}

		} catch (Exception e) {
			System.err.println("Compute exception:");
			e.printStackTrace();
		}
	}
}
