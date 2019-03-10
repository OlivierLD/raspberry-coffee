package utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

public class TCPUtils {

	public static List<String> getIPAddresses() {
		return getIPAddresses(null, false);
	}

	public static List<String> getIPAddresses(String interfaceName) {
		return getIPAddresses(interfaceName, false);
	}

	public static List<String> getIPAddresses(boolean onlyIPv4) {
		return getIPAddresses(null, onlyIPv4);
	}

	private final static Pattern IPV4_PATTERN = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");

	public static List<String> getIPAddresses(String interfaceName, boolean onlyIPv4) {
		List<String> addressList = new ArrayList<>();
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
							addressList.add(String.format("%s %s", iface.getDisplayName(), addr.getHostAddress()));
						}
					}
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		return addressList;
	}

	public static void main(String... args) {
		List<String> addresses = getIPAddresses();
		addresses.stream().forEach(System.out::println);

		// Filtered
		System.out.println("\nFiltered:");
		addresses = getIPAddresses("en0");
		addresses.stream().forEach(System.out::println);
		// IPv4 only
		System.out.println("\nFiltered:");
		addresses = getIPAddresses(true);
		addresses.stream().forEach(System.out::println);
	}
}
