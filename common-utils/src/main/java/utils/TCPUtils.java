package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

public class TCPUtils {

	public static List<String[]> getIPAddresses() {
		return getIPAddresses(null, false);
	}

	public static List<String[]> getIPAddresses(String interfaceName) {
		return getIPAddresses(interfaceName, false);
	}

	public static List<String[]> getIPAddresses(boolean onlyIPv4) {
		return getIPAddresses(null, onlyIPv4);
	}

	private final static Pattern IPV4_PATTERN = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");

	public static List<String[]> getIPAddresses(String interfaceName, boolean onlyIPv4) {
		List<String[]> addressList = new ArrayList<>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (interfaceName == null || iface.getDisplayName().equals(interfaceName)) {
						if (!onlyIPv4 || (onlyIPv4 && IPV4_PATTERN.matcher(addr.getHostAddress()).matches())) {
							addressList.add(new String[] {iface.getDisplayName(), addr.getHostAddress()});
						}
					}
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		return addressList;
	}

	/**
	 * Works on Linux
	 *
	 * On Mac, try $ networksetup -listallhardwareports
	 *          or $ networksetup -getairportnetwork en0
	 *
	 * @return
	 * @throws Exception
	 */
	public static List<String> getNetworkName() throws Exception {
		List<String> networkList = new ArrayList<>();
		String command = "iwconfig"; // "iwconfig | grep wlan0 | awk '{ print $4 }'";
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";
		while (line != null) {
			line = reader.readLine();
			if (line != null) {
				if (line.indexOf("ESSID:") > -1) {
					networkList.add(line.substring(line.indexOf("ESSID:")));
				}
			}
		}
		reader.close();
		return networkList;
	}

	public static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
      return null;
		}
	}

	public static void main(String... args) {
		List<String[]> addresses = getIPAddresses();
		addresses.stream().forEach(pair -> {
			System.out.println(String.format("%s -> %s", pair[0], pair[1]));
		});

		// Filtered
		System.out.println("\nFiltered:");
		addresses = getIPAddresses("en0");
		addresses.stream().forEach(pair -> {
			System.out.println(String.format("%s -> %s", pair[0], pair[1]));
		});
		// IPv4 only
		System.out.println("\nFiltered:");
		addresses = getIPAddresses(true);
		addresses.stream().forEach(pair -> {
			System.out.println(String.format("%s -> %s", pair[0], pair[1]));
		});
	}
}
