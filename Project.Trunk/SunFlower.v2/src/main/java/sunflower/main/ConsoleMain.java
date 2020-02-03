package sunflower.main;

import org.fusesource.jansi.AnsiConsole;
import sunflower.SunFlowerDriver;
import sunflower.utils.EscapeSeq;

import static sunflower.utils.EscapeSeq.*;

public class ConsoleMain {
	/**
	 * System properties:
	 * rpm, default 30
	 * hat.debug, default false
	 * motor.hat.verbose default false
	 * astro.verbose default false
	 * device.lat set default device latitude (useful when no GPS)
	 * device.lng set default device longitude (useful when no GPS)
	 * azimuth.ratio for the azimuth (vertical axis) gear ratio, like "X:Y", ex: "1:40"
	 * elevation.ratio for the elevation (horizontal axis) gear ratio, like "X:Y", ex: "18:128"
	 *
	 * @param args Not used
	 * @throws Exception if anything fails...
	 */
	public static void main(String... args) throws Exception {

		AnsiConsole.systemInstall();
		AnsiConsole.out.println(EscapeSeq.ANSI_CLS);

		SunFlowerDriver sunFlowerDriver = new SunFlowerDriver();

		sunFlowerDriver.subscribe(new SunFlowerDriver.SunFlowerEventListener() {

			private SunFlowerDriver.EventType lastMessageType = null;

			@Override
			public void newMessage(SunFlowerDriver.EventType messageType, String messageContent) {
				int index = SunFlowerDriver.getTypeIndex(messageType);
				AnsiConsole.out.println(ansiLocate(0, index + 1) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + messageContent);
			}
		});

		System.out.println("Hit Ctrl-C to stop the program");

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nShutting down, releasing resources.");
			sunFlowerDriver.stop();
			try { Thread.sleep(5_000); } catch (Exception absorbed) {
				System.err.println("Ctrl-C: Oops!");
				absorbed.printStackTrace();
			}
		}, "Shutdown Hook"));

		String strLat = System.getProperty("device.lat");
		String strLng = System.getProperty("device.lng");
		if (strLat != null && strLng != null) {
			try {
				double lat = Double.parseDouble(strLat);
				double lng = Double.parseDouble(strLng);
				sunFlowerDriver.setDevicePosition(lat, lng);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		// Gear Ratios:
		String zRatioStr = System.getProperty("azimuth.ratio");
		if (zRatioStr != null) {
			String[] zData = zRatioStr.split(":");
			if (zData.length != 2) {
				throw new IllegalArgumentException(String.format("Expecting a value like '1:234', not %s", zRatioStr));
			}
			try {
				double num = Double.parseDouble(zData[0]);
				double den = Double.parseDouble(zData[1]);
				SunFlowerDriver.azimuthMotorRatio = num / den;
			} catch (NumberFormatException nfe) {
				System.err.println("Bad value");
				throw nfe;
			}
		}
		String elevRatioStr = System.getProperty("elevation.ratio");
		if (elevRatioStr != null) {
			String[] elevData = elevRatioStr.split(":");
			if (elevData.length != 2) {
				throw new IllegalArgumentException(String.format("Expecting a value like '1:234', not %s", elevRatioStr));
			}
			try {
				double num = Double.parseDouble(elevData[0]);
				double den = Double.parseDouble(elevData[1]);
				SunFlowerDriver.elevationMotorRatio = num / den;
			} catch (NumberFormatException nfe) {
				System.err.println("Bad value");
				throw nfe;
			}
		}

		sunFlowerDriver.go();

		System.out.println("Bye!");
		AnsiConsole.systemUninstall();
	}}
