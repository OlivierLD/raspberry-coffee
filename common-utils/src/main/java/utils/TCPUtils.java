package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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

	private static String getCommandResult(String command) throws Exception {
		List<String> commands = Arrays.asList("/bin/bash", "-c", command);
		Process p = Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]));
		p.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";
		String result = "";
		while (line != null) {
			line = reader.readLine();
			if (line != null) {
				System.out.println(line);
				result = line;
			}
		}
		return result;
	}

	public static String getDirectoryListing() throws Exception {
		String command = "ls -lisah";
		return getCommandResult(command);
	}

	public static String getUname() throws Exception {
		String command = "uname -a | awk '{print $2}'";
		return getCommandResult(command);
	}

	/*
	 * On Linux
	 */
	public static String getIPAddress() throws Exception {
		String command = "hostname -I | cut -d' ' -f1";
		return getCommandResult(command);
	}

	public static String getCPUTemperature() throws Exception {
		String command = "cat /sys/class/thermal/thermal_zone0/temp |  awk '{printf \"CPU Temp: %.1f C\", $(NF-0) / 1000}'";
		return getCommandResult(command);
	}

	public static String getCPULoad() throws Exception {
		String command = "top -bn1 | grep load | awk '{printf \"CPU Load: %.2f%%\", $(NF-2)*100}'";
		return getCommandResult(command);
	}

	public static String getMemoryUsage() throws Exception {
		String command = "free -m | awk 'NR==2{printf \"Mem: %s/%s MB %.2f%%\", $3, $2, $3*100/$2 }'";
		return getCommandResult(command);
	}

	public static String getDiskUsage() throws Exception {
		String command = "df -h | awk '$NF==\" \"{printf \"Disk: %d/%d GB %s\", $3, $2, $5}'";
		return getCommandResult(command);
	}

	public static void main(String... args) throws Exception {
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

		System.out.println(String.format("DiskUsage: %s", getDiskUsage()));
		System.out.println(String.format("uname: %s", getUname()));
		System.out.println(String.format("ls: %s", getDirectoryListing()));
	}
}
