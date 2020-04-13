package sample.mqtt;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Random;

public class Utils {

	private static final Random random = new Random();

	public static String getMacAddress() {
		String result = "";

		try {
			for (NetworkInterface ni : Collections.list(
							NetworkInterface.getNetworkInterfaces())) {
				byte[] hardwareAddress = ni.getHardwareAddress();

				if (hardwareAddress != null) {
					for (int i = 0; i < hardwareAddress.length; i++)
						result += String.format((i == 0 ? "" : "-") + "%02X", hardwareAddress[i]);

					return result;
				}
			}

		} catch (SocketException e) {
			System.out.println("Could not find out MAC Adress. Exiting Application ");
			System.exit(1);
		}
		return result;
	}

	public static int createRandomNumberBetween(int min, int max) {

		return random.nextInt(max - min + 1) + min;
	}
}