package diozerotests;

import com.diozero.api.PinInfo;
import com.diozero.internal.spi.NativeDeviceFactoryInterface;
import com.diozero.sbc.BoardInfo;
import com.diozero.sbc.DeviceFactoryHelper;
import com.diozero.sbc.LocalSystemInfo;
import com.diozero.util.Diozero;
import com.diozero.util.StringUtil;
import org.fusesource.jansi.AnsiConsole;
import org.tinylog.Logger;

import java.util.Map;
import java.util.stream.Collectors;

import static com.diozero.sampleapps.util.ConsoleUtil.*;
import static org.fusesource.jansi.Ansi.ansi;

public class SystemInformation {
    private static final int MIN_PIN_NAME_LENGTH = 8;

    public static void main(String... args) {
        // Attempt to initialise Jansi
        try {
            AnsiConsole.systemInstall();
        } catch (Throwable t) {
            // Ignore
            Logger.trace(t, "Jansi native library not available on this platform: {}", t);
        }

        // System.out.println(Ansi.ansi().eraseScreen().render("@|red Hello|@ @|green
        // World|@") );

        LocalSystemInfo sys_info = LocalSystemInfo.getInstance();
        System.out.println(ansi().render("@|bold,underline Local System Info|@"));
		/*-
		// Can't do this until JAnsi supports ARMv6:
		AnsiConsole.out().format(render("@|bold diozero version|@: %s%n"), Diozero.getVersion());
		*/
        System.out.format(ansi().render("@|bold diozero version|@: %s%n").toString(), Diozero.getVersion());
        System.out.format(ansi().render("@|bold Operating System|@: %s %s - %s%n").toString(),
                sys_info.getOperatingSystemId(), sys_info.getOperatingSystemVersion(), sys_info.getOsArch());
        System.out.format(ansi().render("@|bold CPU Temperature|@: %.2f%n").toString(),
                Float.valueOf(sys_info.getCpuTemperature()));
        System.out.println();

        try (NativeDeviceFactoryInterface ndf = DeviceFactoryHelper.getNativeDeviceFactory()) {
            BoardInfo board_info = ndf.getBoardInfo();
            System.out.println(ansi().render("@|bold,underline Detected Board Info|@"));
            System.out.format(ansi().render("@|bold Device Factory|@: %s%n").toString(), ndf.getName());
            System.out.format(ansi().render("@|bold Board|@: %s (RAM: %,d bytes, O/S: %s %s)%n").toString(),
                    board_info.getName(), Integer.valueOf(board_info.getMemoryKb()), board_info.getOperatingSystemId(),
                    board_info.getOperatingSystemVersion());
            System.out.format(ansi().render("@|bold I2C Bus Numbers|@: %s%n").toString(),
                    ndf.getI2CBusNumbers().stream().map(Object::toString).collect(Collectors.joining(", ")));

            System.out.println();
            for (Map.Entry<String, Map<Integer, PinInfo>> header_pins_entry : board_info.getHeaders().entrySet()) {
                // Get the maximum pin name length
                int max_length = Math.max(MIN_PIN_NAME_LENGTH, header_pins_entry.getValue().values().stream()
                        .mapToInt(pin_info -> pin_info.getName().length()).max().orElse(MIN_PIN_NAME_LENGTH));

                String name_dash = StringUtil.repeat('-', max_length);
                System.out.format(ansi().render("@|bold Header|@: %s%n").toString(), header_pins_entry.getKey());
                System.out.format("+-----+-%s-+--------+----------+--------+-%s-+-----+%n", name_dash, name_dash);
                System.out.format(ansi().render("+ @|bold GP#|@ + @|bold %" + max_length
                        + "s|@ +  @|bold gpiod|@ + @|bold Physical|@ + @|bold gpiod|@  + @|bold %-" + max_length
                        + "s|@ + @|bold GP#|@ +%n").toString(), "Name", "Name");
                System.out.format("+-----+-%s-+--------+----------+--------+-%s-+-----+%n", name_dash, name_dash);

                Map<Integer, PinInfo> pins = header_pins_entry.getValue();
                int index = 0;
                for (PinInfo pin_info : pins.values()) {
                    if (index++ % 2 == 0) {
                        System.out.format(
                                ansi().render("| @|bold,FG_" + getPinColour(pin_info) + " %3s|@ | @|bold,FG_"
                                        + getPinColour(pin_info) + " %" + max_length
                                        + "s|@ | @|bold %6s|@ | @|bold %2s|@ |").toString(),
                                getNotDefined(pin_info.getDeviceNumber()), pin_info.getName(),
                                getGpiodName(pin_info.getChip(), pin_info.getLineOffset()),
                                getNotDefined(pin_info.getPhysicalPin()));
                    } else {
                        System.out.format(ansi()
                                        .render("| @|bold %-2s|@ | @|bold %-6s|@ | @|bold,FG_" + getPinColour(pin_info) + " %-"
                                                + max_length + "s|@ | @|bold,FG_" + getPinColour(pin_info) + " %-3s|@ |%n")
                                        .toString(), getNotDefined(pin_info.getPhysicalPin()),
                                getGpiodName(pin_info.getChip(), pin_info.getLineOffset()), pin_info.getName(),
                                getNotDefined(pin_info.getDeviceNumber()));
                    }
                }
                if (index % 2 == 1) {
                    System.out.format("|    |        | %-" + max_length + "s |     |%n", "");
                }

                System.out.format("+-----+-%s-+--------+----------+--------+-%s-+-----+%n", name_dash, name_dash);
                System.out.println();
            }
        } finally {
            Diozero.shutdown();
        }
    }
}
