package utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class TCPUtils {

	public static List<String> getIPAddresses() {
		return getIPAddresses(null);
	}

	public static List<String> getIPAddresses(String interfaceName) {
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
						addressList.add(String.format("%s %s", iface.getDisplayName(), addr.getHostAddress()));
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
	}
}
