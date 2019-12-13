package joystick;

import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.i2c.I2CFactory;
import joystick.adc.JoyStick;
import joystick.adc.JoyStickClient;

import servo.StandardServo;

import static utils.TimeUtil.delay;

/**
 * +------------------------------+
 * | JoyStick + MCP3008 + PCA9685 |
 * +------------------------------+
 * <p>
 * Joystick read with ADC (MCP3008)
 * 2 Servos (UP/LR)
 *
 * Parameters are optional.
 * Usage is:
 *   [sudo] java -cp XXX -Dverbose=true|false raspisamples.PanTiltJoyStick -ud:14 -lr:15 -adcUD:0 -adcLR:1
 */
public class PanTiltJoyStick {
	private static StandardServo ssUD = null,
					                     ssLR = null;

	private final static String UD_PREFIX = "-ud:";
	private final static String LR_PREFIX = "-lr:";

	private final static String ADC_UD_PREFIX = "-adcUD:";
	private final static String ADC_LR_PREFIX = "-adcLR:";

	private static MCPReader.MCP3008InputChannels getChannelByNumber(int num) {
		MCPReader.MCP3008InputChannels channel = null;
		for (MCPReader.MCP3008InputChannels ch : MCPReader.MCP3008InputChannels.values()) {
			if (ch.ch() == num) {
				channel = ch;
				break;
			}
		}
		return channel;
	}

	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {

		int ud = 14, lr = 15; // Default
		MCPReader.MCP3008InputChannels adcUD = MCPReader.MCP3008InputChannels.CH0;
		MCPReader.MCP3008InputChannels adcLR = MCPReader.MCP3008InputChannels.CH1;

		if (args.length > 0) {
			for (int i=0; i<args.length; i++) {
				if (args[i].startsWith(UD_PREFIX)) {
					String val = args[i].substring(UD_PREFIX.length());
					try {
						ud = Integer.parseInt(val);
						if (ud < 0 || ud > 15) {
							throw new IllegalArgumentException("Channel must be in [0..15]");
						}
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (args[i].startsWith(LR_PREFIX)) {
					String val = args[i].substring(LR_PREFIX.length());
					try {
						lr = Integer.parseInt(val);
						if (lr < 0 || lr > 15) {
							throw new IllegalArgumentException("Channel must be in [0..15]");
						}
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (args[i].startsWith(ADC_UD_PREFIX)) {
					String val = args[i].substring(ADC_UD_PREFIX.length());
					try {
						int ch = Integer.parseInt(val);
						if (ch < 0 || ch > 7) {
							throw new IllegalArgumentException("ADC Channel must be in [0..7]");
						}
						adcUD = getChannelByNumber(ch);
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (args[i].startsWith(ADC_LR_PREFIX)) {
					String val = args[i].substring(ADC_LR_PREFIX.length());
					try {
						int ch = Integer.parseInt(val);
						if (ch < 0 || ch > 7) {
							throw new IllegalArgumentException("ADC Channel must be in [0..7]");
						}
						adcLR = getChannelByNumber(ch);
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else {
					System.out.println("Un-recognized parameter " + args[i]);
				}
			}
		}

		ssUD = new StandardServo(ud); // 14 : Address on the board (1..15)
		ssLR = new StandardServo(lr); // 15 : Address on the board (1..15)

		// Init/Reset
		ssUD.stop();
		ssLR.stop();
		ssUD.setAngle(0f);
		ssLR.setAngle(0f);

		delay(2_000);

		JoyStickClient jsc = new JoyStickClient() {
			@Override
			public void setUD(int v) { // 0..100
				float angle = (float) (v - 50) * (9f / 5f); // conversion from 1..100 to -90..+90
				if ("true".equals(System.getProperty("verbose", "false")))
					System.out.println("UD:" + v + ", -> " + angle + " deg.");
				ssUD.setAngle(angle); // -90..+90
			}

			@Override
			public void setLR(int v) { // 0..100
				float angle = (float) (v - 50) * (9f / 5f); // conversion from 1..100 to -90..+90
				if ("true".equals(System.getProperty("verbose", "false")))
					System.out.println("LR:" + v + ", -> " + angle + " deg.");
				ssLR.setAngle(angle); // -90..+90
			}
		};

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			ssUD.setAngle(0f);
			ssLR.setAngle(0f);
			delay(500);
			ssUD.stop();
			ssLR.stop();
			System.out.println("\nBye (Ctrl+C)");
		}, "Shutdown Hook"));

		try {
			new JoyStick(jsc, adcUD, adcLR);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ssUD.setAngle(0f);
			ssLR.setAngle(0f);
			delay(500);
			ssUD.stop();
			ssLR.stop();
			System.out.println("Bye");
		}
	}
}
