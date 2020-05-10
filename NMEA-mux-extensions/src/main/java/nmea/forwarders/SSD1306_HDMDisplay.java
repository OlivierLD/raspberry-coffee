package nmea.forwarders;

import lcd.ScreenBuffer;
import nmea.parser.HDG;
import nmea.parser.StringGenerator;
import nmea.parser.StringParsers;
import java.util.List;

/**
 * This is an example of a <b>transformer</b>.
 * <br>
 * Its features are VERY basic and simple:
 * It display the Heading, Pitch and roll from a magnetometer (like HMC5883L or LSM303)
 * on a small OLED Screen (SSD1306).
 *
 * <br>
 * To be used with other apps.
 * This transformer displays the HDM on an OLED display (SSD1306), in its I2C version
 * <br>
 * See http://raspberrypi.lediouris.net/SSD1306/readme.html
 *
 * <br>
 * This is JUST an example. As such, it can be set only from the properties file
 * used at startup. It - for now - cannot be managed from the Web UI.
 * The REST api is not aware of it.
 *
 */
public class SSD1306_HDMDisplay extends SSD1306Processor /* implements Forwarder */ {
	private double declination = 0d;

	private double heading = 0;
	private double pitch = 0d;
	private double roll = 0d;

	private static SSD1306_HDMDisplay instance = null;

	public static SSD1306_HDMDisplay getInstance() {
		return instance;
	}

	public SSD1306_HDMDisplay() throws Exception {
//		System.out.println("Creating instance of SSD1306_HDMDisplay");
		instance = this;
	}

	@Override
	public void init() {

		this.initPartOne();

		sb.text("Starting Forwarder...", 2, 10);
		try {
			if (oled != null) {
				oled.setBuffer(sb.getScreenBuffer());
				oled.display();
			} else {
				substitute.setBuffer(sb.getScreenBuffer());
				substitute.display();
			}
		} catch (Throwable error) {
			error.printStackTrace();
		}
	}

	@Override
	public void write(byte[] message) {
		String str = new String(message);
//	System.out.println(">>>> Mess:" + str);
		if (StringParsers.validCheckSum(str)) {
//		String deviceId = StringParsers.getDeviceID(str);
			String sentenceId = StringParsers.getSentenceID(str);
			boolean toRefresh = false;
			switch (sentenceId) {

				case "HDG":
					HDG hdg = StringParsers.parseHDG(str);
					this.heading = hdg.getHeading();
					if (hdg.getVariation() != -Double.MAX_VALUE && hdg.getVariation() != 0d) {
						this.declination = hdg.getVariation();
					}
					this.heading -= this.declination;
					while (this.heading < 0) {
						this.heading += 360;
					}
					if (verbose) {
						System.out.println("Say something smart");
					}
					toRefresh = true;
					break;
				case "HDM":
					this.heading = StringParsers.parseHDM(str);
					this.heading -= this.declination; // h is true heading
					while (this.heading < 0) {
						this.heading += 360;
					}
					if (verbose) {
						System.out.println("Something smart");
					}
					toRefresh = true;
					break;
				case "XDR":
					// Pitch & Roll
					List<StringGenerator.XDRElement> xdrElements = StringParsers.parseXDR(str);
					for (StringGenerator.XDRElement xdr : xdrElements) {
						switch (xdr.getTransducerName()) {
							case "PTCH":
								this.pitch = xdr.getValue();
								toRefresh = true;
								break;
							case "ROLL":
								this.roll = xdr.getValue();
								toRefresh = true;
								break;
							default:
								break;
						}
					}
					if (verbose && toRefresh) {
						System.out.println("Something smart");
					}
					break;
				default:
					break;
			}
			if (toRefresh && !externallyOwned) {
				refreshDisplay();
			}
		}
	}

	private void refreshDisplay() {
		try {
			sb.clear();
			int line = 0;
			String display = String.format("Heading: %06.02f  ", this.heading);
			sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);
			line += 1;
			display =        String.format("Pitch  :  %05.02f  ", this.pitch);
			sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);
			line += 1;
			display =        String.format("Roll   :  %05.02f  ", this.roll);
			sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Circle, needle pointing north. If heading is 30, north is -30.
			// Center is (128 - 16, 16).
			double heading = this.heading;
			int centerX = this.width - (this.height / 2);
			int centerY = (this.height / 2);
			int radius = (this.height / 2) - 1;
			sb.circle(centerX, centerY, radius);

			// Needle points coordinates
			// Needle front (N)
			int needleTipX = centerX + (int)Math.round(Math.sin(Math.toRadians(- heading)) * radius);
			int needleTipY = centerY - (int)Math.round(Math.cos(Math.toRadians(- heading)) * radius);
			// Needle back (S)
			int needleBackTipX = centerX - (int)Math.round(Math.sin(Math.toRadians(- heading)) * radius);
			int needleBackTipY = centerY + (int)Math.round(Math.cos(Math.toRadians(- heading)) * radius);
			// Needle right side
			int needleRightTipX = centerX + (int)Math.round(Math.sin(Math.toRadians(- heading + 90)) * 3);
			int needleRightTipY = centerY - (int)Math.round(Math.cos(Math.toRadians(- heading + 90)) * 3);
			// Needle left side
			int needleLeftTipX = centerX + (int)Math.round(Math.sin(Math.toRadians(- heading - 90)) * 3);
			int needleLeftTipY = centerY - (int)Math.round(Math.cos(Math.toRadians(- heading - 90)) * 3);

			// Draw compass needle
			sb.line(centerX, centerY, needleTipX, needleTipY);                         // Center to N
			sb.line(needleBackTipX, needleBackTipY, needleRightTipX, needleRightTipY); // S to right
			sb.line(needleRightTipX, needleRightTipY, needleTipX, needleTipY);         // Right to N
			sb.line(needleTipX, needleTipY, needleLeftTipX, needleLeftTipY);           // N to left
			sb.line(needleLeftTipX, needleLeftTipY, needleBackTipX, needleBackTipY);   // left to S

			if (oled != null) {
				oled.setBuffer(sb.getScreenBuffer());
				oled.display();
			} else {
				substitute.setBuffer(sb.getScreenBuffer());
				substitute.display();
			}

			if (verbose) {
				System.out.println(String.format("Heading: %06.02f, Pitch: %05.02f, Roll: %05.02f",
						this.heading,
						this.pitch,
						this.roll));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static class OLEDI2CHDMBean {
		private String cls; // Class
		private String type = "oled-i2c-hdm";

		public OLEDI2CHDMBean(SSD1306_HDMDisplay instance) {
			cls = instance.getClass().getName();
		}
	}

	@Override
	public Object getBean() {
		return new OLEDI2CHDMBean(this);
	}

}
