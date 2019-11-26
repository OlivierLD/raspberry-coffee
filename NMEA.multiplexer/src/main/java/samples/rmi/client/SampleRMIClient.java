package samples.rmi.client;

import context.NMEADataCache;
import nmea.forwarders.rmi.ServerInterface;
import nmea.forwarders.rmi.clientoperations.BoatPosition;
import nmea.forwarders.rmi.clientoperations.CalculatedCurrent;
import nmea.forwarders.rmi.clientoperations.InstantCurrent;
import nmea.forwarders.rmi.clientoperations.LastString;
import nmea.forwarders.rmi.clientoperations.NMEACache;
import nmea.forwarders.rmi.clientoperations.TrueWind;
import nmea.parser.Current;
import nmea.parser.GeoPos;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.NumberFormat;

/**
 * An example of the way to invoke operations on the RMI server
 */
public class SampleRMIClient {

	private static String bindingName = "RMIServer";

	// For the MUX: olediouris-mbp/10.10.226.181, port 1099, name RMI-NMEA
	public static void main(String... args) {
		String name;
		String port;

		if (args.length != 2) {
			System.out.println("Arguments: [RMI Server Name] [Port]");
			name = "raspberrypi3.att.net"; // "olediouris-mbp"; //
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
			ServerInterface comp = (ServerInterface) remote; // registry.lookup(bindingName);
			long after = System.currentTimeMillis();
			System.out.println(String.format("Lookup took %s ms.", NumberFormat.getInstance().format(after - before)));

			LastString task = new LastString();
			before = System.currentTimeMillis();
			String last = comp.executeTask(task);
			after = System.currentTimeMillis();
			System.out.println(String.format("LastString execution took %s ms.", NumberFormat.getInstance().format(after - before)));
			System.out.println(last);

			try { Thread.sleep(1_000L); } catch (InterruptedException ie) {}

			NMEACache cacheTask = new NMEACache();
			before = System.currentTimeMillis();
			NMEADataCache cache = null;
			try {
				cache = comp.executeTask(cacheTask);;
			} catch (Exception e) {
				e.printStackTrace();
			}
			after = System.currentTimeMillis();
			System.out.println(String.format("NMEACache execution took %s ms.", NumberFormat.getInstance().format(after - before)));
			if (cache != null) {
				Object position = cache.get(NMEADataCache.POSITION);
				System.out.println("Position is a " + position.getClass().getName());
				if (position instanceof GeoPos) {
					System.out.println(String.format("Position is %s (Grid Square %s)", ((GeoPos) position).toString(), ((GeoPos) position).gridSquare()));
				}
			}

			try { Thread.sleep(1_000L); } catch (InterruptedException ie) {}

			BoatPosition boatPositionTask = new BoatPosition();
			before = System.currentTimeMillis();
			GeoPos boatGeoPos = comp.executeTask(boatPositionTask);
			after = System.currentTimeMillis();
			System.out.println(String.format("BoatPosition execution took %s ms.", NumberFormat.getInstance().format(after - before)));
			System.out.println(String.format("Position is %s (Grid Square %s)", boatGeoPos.toString(), boatGeoPos.gridSquare()));

			try { Thread.sleep(1_000L); } catch (InterruptedException ie) {}

			TrueWind trueWind = new TrueWind();
			CalculatedCurrent calculatedCurrent = new CalculatedCurrent();
			InstantCurrent instantCurrent = new InstantCurrent();
			// Instant: CSP & CDR
			for (int i=0; i<5; i++) {
				before = System.currentTimeMillis();
				try {
					nmea.parser.TrueWind tw = comp.executeTask(trueWind);
					after = System.currentTimeMillis();
					System.out.println(String.format("TrueWind execution took %s ms.", NumberFormat.getInstance().format(after - before)));
					System.out.println(String.format("TW is %f knots, from %d", tw.getSpeed(), tw.getAngle()));
				} catch (Exception ex) {
					System.err.println("Ooops! TW:" + ex.toString());
				}
				try {
					Current calc = comp.executeTask(calculatedCurrent);
					Current inst = comp.executeTask(instantCurrent);

					System.out.println(String.format("Instant Current    %f knots, dir %d", inst.speed, inst.angle));
					System.out.println(String.format("Calculated Current %f knots, dir %d", calc.speed, calc.angle));
				} catch (Exception ex) {
					System.err.println("Ooops! Current:" + ex.toString());
				}
				try { Thread.sleep(1_000L); } catch (InterruptedException ie) {}
			}
			// Finish with the whole cache
			before = System.currentTimeMillis();
			cache = null;
			try {
				cache = comp.executeTask(cacheTask);;
			} catch (Exception e) {
				e.printStackTrace();
			}
			after = System.currentTimeMillis();
			System.out.println(String.format("NMEACache execution took %s ms.", NumberFormat.getInstance().format(after - before)));
			if (cache != null) {
				Object position = cache.get(NMEADataCache.POSITION);
				System.out.println("Position is a " + position.getClass().getName());
				if (position instanceof GeoPos) {
					System.out.println(String.format("Position is %s (Grid Square %s)", ((GeoPos) position).toString(), ((GeoPos) position).gridSquare()));
				}
			}

		} catch (Exception e) {
			System.err.println("Compute exception:");
			e.printStackTrace();
		}

		System.out.println("Done.");
	}
}
