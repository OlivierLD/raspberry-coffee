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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	 * On Raspberry Pi, also try SystemInfo methods.
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
//				System.out.println(line);
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
		String command = "df -h | awk '$NF==\"/\"{printf \"Disk: %d/%d GB %s\", $3, $2, $5}'";
		return getCommandResult(command);
	}

	// Raspberry Pi temperature, voltage
	// /opt/vc/bin/vcgencmd measure_temp
	// /opt/vc/bin/vcgencmd measure_volts [core | sdram_c | sdram_i | sdram_p]

	public static String getCPUTemperature2() throws Exception {
		String command = "/opt/vc/bin/vcgencmd measure_temp";
		return getCommandResult(command);
	}

	public static String getCoreVoltage() throws Exception {
		String command = "/opt/vc/bin/vcgencmd measure_volts core";
		return getCommandResult(command);
	}

	private final static Map<String, String[]> matrix = new HashMap<>();
	static { // Revision, Release Date, Model, PCB Revision, Memory, Notes
		matrix.put("Beta", new String[] {"Q1 2012",	"B (Beta)",	"?",	"256 MB", "Beta Board"});
		matrix.put("0002", new String[] {"Q1 2012", "B", "1.0", "256 MB", ""});
		matrix.put("0003", new String[] {"Q3 2012", "B (ECN0001)", "1.0", "256 MB", "Fuses mod and D14 removed"});
		matrix.put("0004", new String[] {"Q3 2012", "B", "2.0", "256 MB", "(Mfg by Sony)"});
		matrix.put("0005", new String[] {"Q4 2012", "B", "2.0", "256 MB", "(Mfg by Qisda)"});
		matrix.put("0006", new String[] {"Q4 2012", "B", "2.0", "256 MB", "(Mfg by Egoman)"});
		matrix.put("0007", new String[] {"Q1 2013", "A", "2.0", "256 MB", "(Mfg by Egoman)"});
		matrix.put("0008", new String[] {"Q1 2013", "A", "2.0", "256 MB", "(Mfg by Sony)"});
		matrix.put("0009", new String[] {"Q1 2013", "A", "2.0", "256 MB", "(Mfg by Qisda)"});
		matrix.put("000d", new String[] {"Q4 2012", "B", "2.0", "512 MB", "(Mfg by Egoman)"});
		matrix.put("000e", new String[] {"Q4 2012", "B", "2.0", "512 MB", "(Mfg by Sony)"});
		matrix.put("000f", new String[] {"Q4 2012", "B", "2.0", "512 MB", "(Mfg by Qisda)"});
		matrix.put("0010", new String[] {"Q3 2014", "B+", "1.0", "512 MB", "(Mfg by Sony)"});
		matrix.put("0011", new String[] {"Q2 2014", "Compute Module 1", "1.0", "512 MB", "(Mfg by Sony)"});
		matrix.put("0012", new String[] {"Q4 2014", "A+", "1.1", "256 MB", "(Mfg by Sony)"});
		matrix.put("0013", new String[] {"Q1 2015", "B+", "1.2", "512 MB", "(Mfg by Embest)"});
		matrix.put("0014", new String[] {"Q2 2014", "Compute Module 1", "1.0", "512 MB", "(Mfg by Embest)"});
		matrix.put("0015", new String[] {"?", "A+", "1.1", "256 MB / 512 MB", "(Mfg by Embest)"});
		matrix.put("a01040", new String[] {"Unknown", "2 Model B", "1.0", "1 GB", "(Mfg by Sony)"});
		matrix.put("a01041", new String[] {"Q1 2015", "2 Model B", "1.1", "1 GB", "(Mfg by Sony)"});
		matrix.put("a21041", new String[] {"Q1 2015", "2 Model B", "1.1", "1 GB", "(Mfg by Embest)"});
		matrix.put("a22042", new String[] {"Q3 2016", "2 Model B (with BCM2837)", "1.2", "1 GB", "(Mfg by Embest)"});
		matrix.put("900021", new String[] {"Q3 2016", "A+", "1.1", "512 MB", "(Mfg by Sony)"});
		matrix.put("900032", new String[] {"Q2 2016?", "B+", "1.2", "512 MB", "(Mfg by Sony)"});
		matrix.put("900092", new String[] {"Q4 2015", "Zero", "1.2", "512 MB", "(Mfg by Sony)"});
		matrix.put("900093", new String[] {"Q2 2016", "Zero", "1.3", "512 MB", "(Mfg by Sony)"});
		matrix.put("920093", new String[] {"Q4 2016?", "Zero", "1.3", "512 MB", "(Mfg by Embest)"});
		matrix.put("9000c1", new String[] {"Q1 2017", "Zero W", "1.1", "512 MB", "(Mfg by Sony)"});
		matrix.put("a02082", new String[] {"Q1 2016", "3 Model B", "1.2", "1 GB", "(Mfg by Sony)"});
		matrix.put("a020a0", new String[] {"Q1 2017", "Compute Module 3 (and CM3 Lite)", "1.0", "1 GB", "(Mfg by Sony)"});
		matrix.put("a22082", new String[] {"Q1 2016", "3 Model B", "1.2", "1 GB", "(Mfg by Embest)"});
		matrix.put("a32082", new String[] {"Q4 2016", "3 Model B", "1.2", "1 GB", "(Mfg by Sony Japan)"});
		matrix.put("a020d3", new String[] {"Q1 2018", "3 Model B+", "1.3", "1 GB", "(Mfg by Sony)"});
		matrix.put("9020e0", new String[] {"Q4 2018", "3 Model A+", "1.0", "512 MB", "(Mfg by Sony)"});
		matrix.put("a02100", new String[] {"Q1 2019", "Compute Module 3+", "1.0", "1 GB", "(Mfg by Sony)"});
		matrix.put("a03111", new String[] {"Q2 2019", "4 Model B", "1.1", "1 GB", "(Mfg by Sony)"});
		matrix.put("b03111", new String[] {"Q2 2019", "4 Model B", "1.1", "2 GB", "(Mfg by Sony)"});
		matrix.put("c03111", new String[] {"Q2 2019", "4 Model B", "1.1", "4 GB", "(Mfg by Sony)"});
	}

	public final static int RELEASE_IDX = 0;
	public final static int MODEL_IDX = 1;
	public final static int PCB_REV_IDX = 2;
	public final static int MEMORY_IDX = 3;
	public final static int NOTES_IDX = 4;
	public static String[] getRPiHardwareRevision() throws Exception {
		String command = "cat /proc/cpuinfo | grep 'Revision' | awk '{print $3}' | sed 's/^1000//'";
		String result = getCommandResult(command);
		return matrix.get(result);
	}

	public static void main(String... args) throws Exception {

		try {
			String[] hardwareData = getRPiHardwareRevision();
			System.out.println(String.format("Running on\nModel %s\nReleased %s\nPCB Rev %s\nMemory %s\nNotes %s",
					hardwareData[MODEL_IDX],
					hardwareData[RELEASE_IDX],
					hardwareData[PCB_REV_IDX],
					hardwareData[MEMORY_IDX],
					hardwareData[NOTES_IDX]));
			System.out.println();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

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

		// Temperature & voltage
		System.out.println();
		System.out.println(String.format("CPU Temperature %s", getCPUTemperature2()));
		System.out.println(String.format("Cove Voltage %s", getCoreVoltage()));
	}
}
