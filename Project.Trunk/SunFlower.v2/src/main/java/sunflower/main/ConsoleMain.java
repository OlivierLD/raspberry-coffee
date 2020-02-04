package sunflower.main;

import org.fusesource.jansi.AnsiConsole;
import sunflower.SunFlowerDriver;
import sunflower.utils.ANSIUtil;

import static sunflower.utils.ANSIUtil.ANSI_DEFAULT_BACKGROUND;
import static sunflower.utils.ANSIUtil.ANSI_DEFAULT_TEXT;
import static sunflower.utils.ANSIUtil.ANSI_NORMAL;
import static sunflower.utils.ANSIUtil.ansiLocate;

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
		AnsiConsole.out.println(ANSIUtil.ANSI_CLS);

		SunFlowerDriver sunFlowerDriver = new SunFlowerDriver();

		ANSIUtil.printPositionTable();
		ANSIUtil.printMovementTable();
		ANSIUtil.printInfoTable();

		sunFlowerDriver.subscribe(new SunFlowerDriver.SunFlowerEventListener() {

			@Override
			public void newMessage(SunFlowerDriver.EventType messageType, Object messagePayload) {

				String message = "";

				if (messageType.equals(SunFlowerDriver.EventType.CELESTIAL_DATA)) {
					SunFlowerDriver.SunData sunData = (SunFlowerDriver.SunData)messagePayload;
					ANSIUtil.printSunPosDate(sunData.getDate().toString());
					ANSIUtil.printSunPosZ(String.format("%.02f", sunData.getAzimuth()));
					ANSIUtil.printSunPosElev(String.format("%.02f", sunData.getElevation()));
				} else if (messageType.equals(SunFlowerDriver.EventType.DEVICE_DATA)) {
					SunFlowerDriver.DeviceData deviceData = (SunFlowerDriver.DeviceData)messagePayload;
					ANSIUtil.printDevicePosDate(deviceData.getDate().toString());
					ANSIUtil.printDevicePosZ(String.format("%.02f", deviceData.getAzimuth()));
					ANSIUtil.printDevicePosElev(String.format("%.02f", deviceData.getElevation()));
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_ELEVATION_START)) {
					SunFlowerDriver.DeviceElevationStart des = (SunFlowerDriver.DeviceElevationStart) messagePayload;
					ANSIUtil.printElevMovDate(des.getDate().toString());
					ANSIUtil.printElevMovFrom(String.format("%.02f", des.getDeviceElevation()));
					ANSIUtil.printElevMovTo(String.format("%.02f", des.getSunElevation()));
					ANSIUtil.printElevMovDiff(String.format("%.02f", Math.abs(des.getDeviceElevation() - des.getSunElevation())));
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_AZIMUTH_START)) {
					SunFlowerDriver.DeviceAzimuthStart das = (SunFlowerDriver.DeviceAzimuthStart) messagePayload;
					ANSIUtil.printZMovDate(das.getDate().toString());
					ANSIUtil.printZMovFrom(String.format("%.02f", das.getDeviceAzimuth()));
					ANSIUtil.printZMovTo(String.format("%.02f", das.getSunAzimuth()));
					ANSIUtil.printZMovDiff(String.format("%.02f", Math.abs(das.getDeviceAzimuth() - das.getSunAzimuth())));
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_ELEVATION_START_2)) {
					SunFlowerDriver.MoveDetails md = (SunFlowerDriver.MoveDetails) messagePayload;
					message = String.format("%s %s", messageType, md.toString());
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_AZIMUTH_START_2)) {
					SunFlowerDriver.MoveDetails md = (SunFlowerDriver.MoveDetails) messagePayload;
					message = String.format("%s %s", messageType, md.toString());
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_ELEVATION_END)) {
					SunFlowerDriver.MoveCompleted mc = (SunFlowerDriver.MoveCompleted) messagePayload;
					message = String.format("%s %s", messageType, mc.toString());
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_AZIMUTH_END)) {
					SunFlowerDriver.MoveCompleted mc = (SunFlowerDriver.MoveCompleted)messagePayload;
					message = String.format("%s %s", messageType, mc.toString());
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_ELEVATION_INFO)) {
					SunFlowerDriver.DeviceInfo deviceInfo = (SunFlowerDriver.DeviceInfo)messagePayload;
					// TODO Separate cells
					ANSIUtil.printInfoDate(deviceInfo.getDate().toString());
					ANSIUtil.printInfoMessage(deviceInfo.getMessage());
//					message = String.format("%s %s", messageType, messagePayload.toString());
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_AZIMUTH_INFO)) {
					SunFlowerDriver.DeviceInfo deviceInfo = (SunFlowerDriver.DeviceInfo)messagePayload;
					// TODO Separate cells
					ANSIUtil.printInfoDate(deviceInfo.getDate().toString());
					ANSIUtil.printInfoMessage(deviceInfo.getMessage());
//					message = String.format("%s %s", messageType, messagePayload.toString());
				} else if (messageType.equals(SunFlowerDriver.EventType.DEVICE_INFO)) {
					SunFlowerDriver.DeviceInfo deviceInfo = (SunFlowerDriver.DeviceInfo)messagePayload;
					ANSIUtil.printInfoDate(deviceInfo.getDate().toString());
					ANSIUtil.printInfoMessage(deviceInfo.getMessage());
//					message = String.format("%s %s", messageType, messagePayload.toString());
				} else { // Default...
					message = String.format("%s %s", "Default", messagePayload.toString());
				}
				int index = SunFlowerDriver.getTypeIndex(messageType);
				AnsiConsole.out.println(ansiLocate(0, index + 20) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + message);
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
