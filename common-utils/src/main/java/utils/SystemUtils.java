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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class SystemUtils {

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
                            addressList.add(new String[]{iface.getDisplayName(), addr.getHostAddress()});
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
     * <p>
     * On Mac, try $ networksetup -listallhardwareports
     * or $ networksetup -getairportnetwork en0
     * <p>
     * On Raspberry Pi, also try SystemInfo methods.
     *
     * @return the list of networks in sight
     * @throws Exception when iwconfig is not available for example
     */
    public static List<String> getNetworkName() throws Exception {
        final String ESSID_LABEL = "ESSID:";
        List<String> networkList = new ArrayList<>();
        // Needs to be in the PATH. If problem, check if /usr/sbin is in the PATH
        String command = "iwconfig"; // "iwconfig | grep wlan0 | awk '{ print $4 }'";
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while (line != null) {
            line = reader.readLine();
            if (line != null) {
                if (line.contains(ESSID_LABEL)) {
                    networkList.add(line.substring(line.indexOf(ESSID_LABEL) + ESSID_LABEL.length()));
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

    private static List<String> getCommandResult(String command) throws Exception {
        List<String> commands = Arrays.asList("/bin/bash", "-c", command);
        List<String> result = new ArrayList<>();
        Process p = Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]));
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while (line != null) {
            line = reader.readLine();
            if (line != null) {
//				System.out.println(line);
                result.add(line);
            }
        }
        return result;
    }

    public static List<String> getOSDetails() throws Exception {
        String command = "hostnamectl";
        return getCommandResult(command);
    }

    public static List<String> getDirectoryListing() throws Exception {
        String command = "ls -lisah";
        return getCommandResult(command);
    }

    public static String getUname() throws Exception {
        String command = "uname -a | awk '{print $2}'";
        return getCommandResult(command).get(0);
    }

    /*
     * On Linux
     */
    public static String getIPAddress() throws Exception {
        String command = "hostname -I | cut -d' ' -f1";
        return getCommandResult(command).get(0);
    }

    public static String getCPUTemperature() throws Exception {
        String command = "cat /sys/class/thermal/thermal_zone0/temp |  awk '{printf \"CPU Temp: %.1f C\", $(NF-0) / 1000}'";
        return getCommandResult(command).get(0);
    }

    public static String getCPULoad() throws Exception {
        String command = "top -bn1 | grep load | awk '{printf \"CPU Load: %.2f%%\", $(NF-2)*100}'";
        return getCommandResult(command).get(0);
    }

    public static String getCPULoad2() throws Exception {
        // Returns 0 minute, 1 minutes, 5 or 15 minutes CPU activity (allows to see which way it's going)
        String command = "cat /proc/loadavg | awk '{ print $1 }'";
        return getCommandResult(command).get(0);
    }

    public static String getNBCpu() throws Exception {
        String command = "lscpu | grep 'CPU.*s):' | awk '{ print $2 }'";
        return getCommandResult(command).get(0);
    }

    public static String getMemoryUsage() throws Exception {
        String command = "free -m | awk 'NR==2{printf \"Mem: %s/%s MB %.2f%%\", $3, $2, $3*100/$2 }'";
        return getCommandResult(command).get(0);
    }

    public final static int MEM_TOTAL = 0;
    public final static int MEM_FREE = 1;
    public final static int MEM_AVAILABLE = 2;

    public static List<String> getMemoryStatus() throws Exception {
        String[] commands = { // In MB
                "grep MemTotal /proc/meminfo | awk '{print $2 / 1024}'",
                "grep MemFree /proc/meminfo | awk '{print $2 / 1024}'",
                "grep MemAvailable /proc/meminfo | awk '{print $2 / 1024}'"
        };
        return Arrays.asList(
                getCommandResult(commands[0]).get(0),
                getCommandResult(commands[1]).get(0),
                getCommandResult(commands[2]).get(0)
        );
    }

    public static String getDiskUsage() throws Exception {
        String command = "df -h | awk '$NF==\"/\"{printf \"Disk: %d/%d GB %s\", $3, $2, $5}'";
        return getCommandResult(command).get(0);
    }

    // Raspberry Pi temperature, voltage
    // /opt/vc/bin/vcgencmd measure_temp
    // /opt/vc/bin/vcgencmd measure_volts [core | sdram_c | sdram_i | sdram_p]

    public static String getCPUTemperature2() throws Exception {
//		String command = "/opt/vc/bin/vcgencmd measure_temp";
        String command = "vcgencmd measure_temp";
        return getCommandResult(command).get(0);
    }

    public static String getCoreVoltage() throws Exception {
//		String command = "/opt/vc/bin/vcgencmd measure_volts core";
        String command = "vcgencmd measure_volts core";
        return getCommandResult(command).get(0);
    }

    // Find official info at https://www.raspberrypi.org/documentation/hardware/raspberrypi/revision-codes/README.md
    // Also https://www.raspberrypi.com/documentation/computers/raspberry-pi.html#new-style-revision-codes-in-use
    private final static Map<String, String[]> matrix = new HashMap<>(); // Too big for a Map.of
    static { // Revision, Release Date, Model, PCB Revision, Memory, Notes
        matrix.put("Beta", new String[]{"Q1 2012", "B (Beta)", "?", "256 MB", "Beta Board"});
        matrix.put("0002", new String[]{"Q1 2012", "B", "1.0", "256 MB", ""});
        matrix.put("0003", new String[]{"Q3 2012", "B (ECN0001)", "1.0", "256 MB", "Fuses mod and D14 removed"});
        matrix.put("0004", new String[]{"Q3 2012", "B", "2.0", "256 MB", "(Mfg by Sony UK)"});
        matrix.put("0005", new String[]{"Q4 2012", "B", "2.0", "256 MB", "(Mfg by Qisda)"});
        matrix.put("0006", new String[]{"Q4 2012", "B", "2.0", "256 MB", "(Mfg by Egoman)"});
        matrix.put("0007", new String[]{"Q1 2013", "A", "2.0", "256 MB", "(Mfg by Egoman)"});
        matrix.put("0008", new String[]{"Q1 2013", "A", "2.0", "256 MB", "(Mfg by Sony UK)"});
        matrix.put("0009", new String[]{"Q1 2013", "A", "2.0", "256 MB", "(Mfg by Qisda)"});
        matrix.put("000d", new String[]{"Q4 2012", "B", "2.0", "512 MB", "(Mfg by Egoman)"});
        matrix.put("000e", new String[]{"Q4 2012", "B", "2.0", "512 MB", "(Mfg by Sony UK)"});
        matrix.put("000f", new String[]{"Q4 2012", "B", "2.0", "512 MB", "(Mfg by Qisda)"});
        matrix.put("0010", new String[]{"Q3 2014", "B+", "1.0", "512 MB", "(Mfg by Sony UK)"});
        matrix.put("0011", new String[]{"Q2 2014", "Compute Module 1", "1.0", "512 MB", "(Mfg by Sony UK)"});
        matrix.put("0012", new String[]{"Q4 2014", "A+", "1.1", "256 MB", "(Mfg by Sony UK)"});
        matrix.put("0013", new String[]{"Q1 2015", "B+", "1.2", "512 MB", "(Mfg by Embest)"});
        matrix.put("0014", new String[]{"Q2 2014", "Compute Module 1", "1.0", "512 MB", "(Mfg by Embest)"});
        matrix.put("0015", new String[]{"?", "A+", "1.1", "256 MB / 512 MB", "(Mfg by Embest)"});
        matrix.put("a01040", new String[]{"Unknown", "2 Model B", "1.0", "1 GB", "(Mfg by Sony UK)"});
        matrix.put("a01041", new String[]{"Q1 2015", "2 Model B", "1.1", "1 GB", "(Mfg by Sony UK)"});
        matrix.put("a21041", new String[]{"Q1 2015", "2 Model B", "1.1", "1 GB", "(Mfg by Embest)"});
        matrix.put("a22042", new String[]{"Q3 2016", "2 Model B (with BCM2837)", "1.2", "1 GB", "(Mfg by Embest)"});
        matrix.put("900021", new String[]{"Q3 2016", "A+", "1.1", "512 MB", "(Mfg by Sony UK)"});
        matrix.put("900032", new String[]{"Q2 2016?", "B+", "1.2", "512 MB", "(Mfg by Sony UK)"});
        matrix.put("900092", new String[]{"Q4 2015", "Zero", "1.2", "512 MB", "(Mfg by Sony UK)"});
        matrix.put("900093", new String[]{"Q2 2016", "Zero", "1.3", "512 MB", "(Mfg by Sony UK)"});
        matrix.put("920093", new String[]{"Q4 2016?", "Zero", "1.3", "512 MB", "(Mfg by Embest)"});
        matrix.put("9000c1", new String[]{"Q1 2017", "Zero W", "1.1", "512 MB", "(Mfg by Sony UK)"});
        matrix.put("a02082", new String[]{"Q1 2016", "3 Model B", "1.2", "1 GB", "(Mfg by Sony UK)"});
        matrix.put("a020a0", new String[]{"Q1 2017", "Compute Module 3 (and CM3 Lite)", "1.0", "1 GB", "(Mfg by Sony UK)"});
        matrix.put("a22082", new String[]{"Q1 2016", "3 Model B", "1.2", "1 GB", "(Mfg by Embest)"});
        matrix.put("a32082", new String[]{"Q4 2016", "3 Model B", "1.2", "1 GB", "(Mfg by Sony Japan)"});
        matrix.put("a020d3", new String[]{"Q1 2018", "3 Model B+", "1.3", "1 GB", "(Mfg by Sony UK)"});
        matrix.put("9020e0", new String[]{"Q4 2018", "3 Model A+", "1.0", "512 MB", "(Mfg by Sony UK)"});
        matrix.put("a02100", new String[]{"Q1 2019", "Compute Module 3+", "1.0", "1 GB", "(Mfg by Sony UK)"});
        matrix.put("a03111", new String[]{"Q2 2019", "4 Model B", "1.1", "1 GB", "(Mfg by Sony UK)"});
        matrix.put("b03111", new String[]{"Q2 2019", "4 Model B", "1.1", "2 GB", "(Mfg by Sony UK)"});
        matrix.put("c03111", new String[]{"Q2 2019", "4 Model B", "1.1", "4 GB", "(Mfg by Sony UK)"});
        matrix.put("c03112", new String[]{"-------", "4 Model B", "1.2", "4 GB", "(Mfg by Sony UK)"});
        matrix.put("d03114", new String[]{"-------", "4 Model B", "1.4", "8 GB", "(Mfg by Sony UK)"});
        matrix.put("c03130", new String[]{"-------", "Pi 400", "1.0", "4 GB", "(Mfg by Sony UK)"});
    }

    private final static Map<String, String[]> matrix2022 = new HashMap<>(); // Too big for a Map.of
    static { // Revision, Release Date, Model, PCB Revision, Memory, Notes
        matrix2022.put("900021", new String[] { "-------", "A+", "1.1", "512MB", "Sony UK" });
        matrix2022.put("900032", new String[] { "-------", "B+", "1.2", "512MB", "Sony UK" });
        matrix2022.put("900092", new String[] { "-------", "Zero", "1.2", "512MB", "Sony UK" });
        matrix2022.put("900093", new String[] { "-------", "Zero", "1.3", "512MB", "Sony UK" });
        matrix2022.put("9000c1", new String[] { "-------", "Zero W", "1.1", "512MB", "Sony UK" });
        matrix2022.put("9020e0", new String[] { "-------", "3A+", "1.0", "512MB", "Sony UK" });
        matrix2022.put("920092", new String[] { "-------", "Zero", "1.2", "512MB", "Embest" });
        matrix2022.put("920093", new String[] { "-------", "Zero", "1.3", "512MB", "Embest" });
        matrix2022.put("900061", new String[] { "-------", "CM", "1.1", "512MB", "Sony UK" });
        matrix2022.put("a01040", new String[] { "-------", "2B", "1.0", "1GB", "Sony UK" });
        matrix2022.put("a01041", new String[] { "-------", "2B", "1.1", "1GB", "Sony UK" });
        matrix2022.put("a02082", new String[] { "-------", "3B", "1.2", "1GB", "Sony UK" });
        matrix2022.put("a020a0", new String[] { "-------", "CM3", "1.0", "1GB", "Sony UK" });
        matrix2022.put("a020d3", new String[] { "-------", "3B+", "1.3", "1GB", "Sony UK" });
        matrix2022.put("a02042", new String[] { "-------", "2B (with BCM2837)", "1.2", "1GB", "Sony UK" });
        matrix2022.put("a21041", new String[] { "-------", "2B", "1.1", "1GB", "Embest" });
        matrix2022.put("a22042", new String[] { "-------", "2B (with BCM2837)", "1.2", "1GB", "Embest" });
        matrix2022.put("a22082", new String[] { "-------", "3B", "1.2", "1GB", "Embest" });
        matrix2022.put("a220a0", new String[] { "-------", "CM3", "1.0", "1GB", "Embest" });
        matrix2022.put("a32082", new String[] { "-------", "3B", "1.2", "1GB", "Sony Japan" });
        matrix2022.put("a52082", new String[] { "-------", "3B", "1.2", "1GB", "Stadium" });
        matrix2022.put("a22083", new String[] { "-------", "3B", "1.3", "1GB", "Embest" });
        matrix2022.put("a02100", new String[] { "-------", "CM3+", "1.0", "1GB", "Sony UK" });
        matrix2022.put("a03111", new String[] { "-------", "4B", "1.1", "1GB", "Sony UK" });
        matrix2022.put("b03111", new String[] { "-------", "4B", "1.1", "2GB", "Sony UK" });
        matrix2022.put("b03112", new String[] { "-------", "4B", "1.2", "2GB", "Sony UK" });
        matrix2022.put("b03114", new String[] { "-------", "4B", "1.4", "2GB", "Sony UK" });
        matrix2022.put("c03111", new String[] { "-------", "4B", "1.1", "4GB", "Sony UK" });
        matrix2022.put("c03112", new String[] { "-------", "4B", "1.2", "4GB", "Sony UK" });
        matrix2022.put("c03114", new String[] { "-------", "4B", "1.4", "4GB", "Sony UK" });
        matrix2022.put("d03114", new String[] { "-------", "4B", "1.4", "8GB", "Sony UK" });
        matrix2022.put("c03130", new String[] { "-------", "Pi 400", "1.0", "4GB", "Sony UK" });
        matrix2022.put("a03140", new String[] { "-------", "CM4", "1.0", "1GB", "Sony UK" });
        matrix2022.put("b03140", new String[] { "-------", "CM4", "1.0", "2GB", "Sony UK" });
        matrix2022.put("c03140", new String[] { "-------", "CM4", "1.0", "4GB", "Sony UK" });
        matrix2022.put("d03140", new String[] { "-------", "CM4", "1.0", "8GB", "Sony UK" });
        matrix2022.put("902120", new String[] { "-------", "Zero 2 W", "1.0", "512MB", "Sony UK" });
    }

    public final static int RELEASE_IDX = 0;
    public final static int MODEL_IDX = 1;
    public final static int PCB_REV_IDX = 2;
    public final static int MEMORY_IDX = 3;
    public final static int NOTES_IDX = 4;

    private final static boolean USE_2022_DATA = true;

    public static String[] getRPiHardwareRevision() throws Exception {
        String command = "cat /proc/cpuinfo | grep 'Revision' | awk '{print $3}' | sed 's/^1000//'";
        List<String> commandResult = getCommandResult(command);
        if (commandResult.size() > 0) {
            String result = getCommandResult(command).get(0);
            return USE_2022_DATA ?
                    matrix2022.get(result) :
                    matrix.get(result);
        } else {
            return null;
        }
    }

    public static void main(String... args) throws Exception {

        System.out.printf(">> (This is class %s)\n", SystemUtils.class.getName());

        AtomicBoolean minimal = new AtomicBoolean(false);
        AtomicBoolean freeMem = new AtomicBoolean(true);
        Arrays.asList(args).forEach(arg -> {
            if ("--minimal".equals(arg)) {
                minimal.set(true);
            } else if ("--no-free-mem".equals(arg)) {
                freeMem.set(false);
            }
        });

        try {
            String[] hardwareData = getRPiHardwareRevision();
            if (hardwareData != null) {
                if (USE_2022_DATA) {
                    System.out.printf("Running on:\n" +
                                    "          Model: %s\n" +
                                    "        PCB Rev: %s\n" +
                                    "         Memory: %s\n" +
                                    "Manufactured by: %s\n",
                            hardwareData[MODEL_IDX],
                            hardwareData[PCB_REV_IDX],
                            hardwareData[MEMORY_IDX],
                            hardwareData[NOTES_IDX]);
                } else {
                    System.out.printf("Running on:\nModel: %s\nReleased: %s\nPCB Rev: %s\nMemory: %s\nNotes: %s\n",
                            hardwareData[MODEL_IDX],
                            hardwareData[RELEASE_IDX],
                            hardwareData[PCB_REV_IDX],
                            hardwareData[MEMORY_IDX],
                            hardwareData[NOTES_IDX]);
                }
                System.out.println();
            } else {
                System.out.printf(">> No data for this platform. See source of %s\n", SystemUtils.class.getName());
            }
        } catch (IndexOutOfBoundsException iobe) {
            System.out.println("- Unknown - Is that a Raspberry Pi?");
        } catch (Exception ex) {
            System.err.println("Not on a Raspberry Pi?");
            ex.printStackTrace();
        }

        try {
            System.out.printf("OS Details:\n%s\n", String.join("\n", getOSDetails()));
        } catch (Exception ex) {
            System.err.println(ex);
        }

        try {
            List<String> networkName = getNetworkName();
            try {
                networkName.forEach(network -> System.out.printf("Network: %s\n", network));
            } catch (Exception ex) {
                System.err.println(ex);
            }
            System.out.println();
            System.out.println("All IP Addresses:");
            List<String[]> addresses = getIPAddresses();
            addresses.stream().forEach(pair -> System.out.printf("%s -> %s\n", pair[0], pair[1]));
            // Filtered
            System.out.println("\nFiltered (en0):");
            addresses = getIPAddresses("en0");
            addresses.stream().forEach(pair -> System.out.printf("%s -> %s\n", pair[0], pair[1]));
            // IPv4 only
            System.out.println("\nFiltered (IPv4):");
            addresses = getIPAddresses(true);
            addresses.stream().forEach(pair -> System.out.printf("%s -> %s\n", pair[0], pair[1]));
        } catch (Exception ex) {
            System.err.println(ex);
        }

        try {
            System.out.println();
            System.out.printf("DiskUsage: %s\n", getDiskUsage());
            System.out.printf("uname: %s\n", getUname());
            if (!minimal.get()) {
                System.out.println();
                System.out.printf("Directory listing:\n%s\n", String.join("\n", getDirectoryListing()));
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }

        // Temperature & voltage
        try {
            System.out.println();
            System.out.printf("CPU Temperature %s\n", getCPUTemperature2());
            System.out.printf("Core Voltage %s\n", getCoreVoltage());
        } catch (Exception ex) {
            System.err.printf("Maybe not on a Raspberry PI ? Temp & Volt %s\n", ex.toString());
        }

        // Memory
        if (freeMem.get()) {
            try {
                System.out.println();
                List<String> memStatus = getMemoryStatus();
                System.out.printf("Total:     %s MB\n", memStatus.get(MEM_TOTAL));
                System.out.printf("Free:      %s MB\n", memStatus.get(MEM_FREE));
                System.out.printf("Available: %s MB\n", memStatus.get(MEM_AVAILABLE));

                System.out.println();
                String memoryUsage = getMemoryUsage();
                System.out.printf("Usage: %s\n", memoryUsage);
            } catch (Exception ex) {
                System.err.printf("MemStat: %s\n", ex.toString());
            }
        }
    }
}
