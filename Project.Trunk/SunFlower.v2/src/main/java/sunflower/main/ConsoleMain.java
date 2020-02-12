package sunflower.main;

import org.fusesource.jansi.AnsiConsole;
import sunflower.SunFlowerDriver;
import sunflower.utils.ANSIUtil;
import utils.TimeUtil;

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
	 * deltaT
	 * astro.verbose default false
	 * device.lat set default device latitude (useful when no GPS)
	 * device.lng set default device longitude (useful when no GPS)
	 * azimuth.ratio for the azimuth (vertical axis) gear ratio, like "X:Y", ex: "1:40"
	 * elevation.ratio for the elevation (horizontal axis) gear ratio, like "X:Y", ex: "18:128"
	 * ansi.boxes set to true for nicer ANSI tables.
	 * min.diff.for.move like 0.5 (defvault)
	 *
	 * @param args Not used
	 */
	public static void main(String... args) {

		AnsiConsole.systemInstall();
		AnsiConsole.out.println(ANSIUtil.ANSI_CLS);

		SunFlowerDriver sunFlowerDriver = new SunFlowerDriver();

		// Empty tables
		ANSIUtil.printPositionTable();
		ANSIUtil.printMovementTable();
		ANSIUtil.printInfoTable();

		sunFlowerDriver.subscribe(new SunFlowerDriver.SunFlowerEventListener() {

			@Override
			public void onNewMessage(SunFlowerDriver.EventType messageType, Object messagePayload) {

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
					ANSIUtil.printInfoDate2(md.getDate().toString());
					ANSIUtil.printInfoMessage2(String.format("%d steps %s on motor #%d", md.getNbSteps(), md.getMotorCommand(), md.getMotorNum()));
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_AZIMUTH_START_2)) {
					SunFlowerDriver.MoveDetails md = (SunFlowerDriver.MoveDetails) messagePayload;
					ANSIUtil.printInfoDate3(md.getDate().toString());
					ANSIUtil.printInfoMessage3(String.format("%d steps %s on motor #%d", md.getNbSteps(), md.getMotorCommand(), md.getMotorNum()));
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_ELEVATION_END)) {
					SunFlowerDriver.MoveCompleted mc = (SunFlowerDriver.MoveCompleted) messagePayload;
					ANSIUtil.printInfoDate2(mc.getDate().toString());
					ANSIUtil.printInfoMessage2(String.format("Elev.: Move (%d steps) completed in %s", mc.getNbSteps(), TimeUtil.fmtDHMS(TimeUtil.msToHMS(mc.getElapsed()))));
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_AZIMUTH_END)) {
					SunFlowerDriver.MoveCompleted mc = (SunFlowerDriver.MoveCompleted)messagePayload;
					ANSIUtil.printInfoDate3(mc.getDate().toString());
					ANSIUtil.printInfoMessage3(String.format("Z    : Move (%d steps) completed in %s", mc.getNbSteps(), TimeUtil.fmtDHMS(TimeUtil.msToHMS(mc.getElapsed()))));
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_ELEVATION_INFO)) {
					SunFlowerDriver.DeviceInfo deviceInfo = (SunFlowerDriver.DeviceInfo)messagePayload;
					ANSIUtil.printInfoDate2(deviceInfo.getDate().toString());
					ANSIUtil.printInfoMessage2(deviceInfo.getMessage());
				} else if (messageType.equals(SunFlowerDriver.EventType.MOVING_AZIMUTH_INFO)) {
					SunFlowerDriver.DeviceInfo deviceInfo = (SunFlowerDriver.DeviceInfo)messagePayload;
					ANSIUtil.printInfoDate3(deviceInfo.getDate().toString());
					ANSIUtil.printInfoMessage3(deviceInfo.getMessage());
				} else if (messageType.equals(SunFlowerDriver.EventType.DEVICE_INFO)) {
					SunFlowerDriver.DeviceInfo deviceInfo = (SunFlowerDriver.DeviceInfo)messagePayload;
					ANSIUtil.printInfoDate1(deviceInfo.getDate().toString());
					ANSIUtil.printInfoMessage1(deviceInfo.getMessage());
				} else { // Default...
					message = String.format("%s %s", "Default", messagePayload.toString());
				}
				// if (message.trim().length() > 0) {
					int index = SunFlowerDriver.getTypeIndex(messageType);
					AnsiConsole.out.println(ansiLocate(0, index + 20) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + message);
				// }
			}
		});

		sunFlowerDriver.init();
		sunFlowerDriver.start();

		AnsiConsole.systemUninstall();
		System.out.println("Bye!");
	}
}
