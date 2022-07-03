package diozerokt

import com.diozero.sbc.LocalSystemInfo
import com.diozero.util.Diozero
import com.diozero.sbc.DeviceFactoryHelper
//import com.diozero.sbc.BoardInfo
import java.util.stream.Collectors
import com.diozero.api.PinInfo
//import java.util.function.ToIntFunction
import com.diozero.sampleapps.util.ConsoleUtil
import com.diozero.util.StringUtil
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import org.tinylog.Logger

object SystemInformation {
    private const val MIN_PIN_NAME_LENGTH = 8

    @JvmStatic
    fun main(args: Array<String>) {
        // Attempt to initialise Jansi
        try {
            AnsiConsole.systemInstall()
        } catch (t: Throwable) {
            // Ignore
            Logger.trace(t, "Jansi native library not available on this platform: {}", t)
        }

        // System.out.println(Ansi.ansi().eraseScreen().render("@|red Hello|@ @|green
        // World|@") );
        val sys_info = LocalSystemInfo.getInstance()
        println(Ansi.ansi().render("@|bold,underline Local System Info|@"))
        /*-
		// Can't do this until JAnsi supports ARMv6:
		AnsiConsole.out().format(render("@|bold diozero version|@: %s%n"), Diozero.getVersion());
		*/System.out.format(Ansi.ansi().render("@|bold diozero version|@: %s%n").toString(), Diozero.getVersion())
        System.out.format(
            Ansi.ansi().render("@|bold Operating System|@: %s %s - %s%n").toString(),
            sys_info.operatingSystemId, sys_info.operatingSystemVersion, sys_info.osArch
        )
        System.out.format(
            Ansi.ansi().render("@|bold CPU Temperature|@: %.2f%n").toString(),
            java.lang.Float.valueOf(sys_info.cpuTemperature)
        )
        println()
        try {
            DeviceFactoryHelper.getNativeDeviceFactory().use { ndf ->
                val board_info = ndf.boardInfo
                println(Ansi.ansi().render("@|bold,underline Detected Board Info|@"))
                System.out.format(Ansi.ansi().render("@|bold Device Factory|@: %s%n").toString(), ndf.name)
                System.out.format(
                    Ansi.ansi().render("@|bold Board|@: %s (RAM: %,d bytes, O/S: %s %s)%n").toString(),
                    board_info.name, Integer.valueOf(board_info.memoryKb), board_info.operatingSystemId,
                    board_info.operatingSystemVersion
                )
                System.out.format(
                    Ansi.ansi().render("@|bold I2C Bus Numbers|@: %s%n").toString(),
                    ndf.i2CBusNumbers.stream().map { obj: Int -> obj.toString() }.collect(Collectors.joining(", "))
                )
                println()
                for ((key, pins) in board_info.headers) {
                    // Get the maximum pin name length
                    val max_length = Math.max(MIN_PIN_NAME_LENGTH, pins.values.stream()
                        .mapToInt { pin_info: PinInfo -> pin_info.name.length }.max().orElse(MIN_PIN_NAME_LENGTH)
                    )
                    val name_dash = StringUtil.repeat('-', max_length)
                    System.out.format(Ansi.ansi().render("@|bold Header|@: %s%n").toString(), key)
                    System.out.format("+-----+-%s-+--------+----------+--------+-%s-+-----+%n", name_dash, name_dash)
                    System.out.format(
                        Ansi.ansi().render(
                            "+ @|bold GP#|@ + @|bold %" + max_length
                                    + "s|@ +  @|bold gpiod|@ + @|bold Physical|@ + @|bold gpiod|@  + @|bold %-" + max_length
                                    + "s|@ + @|bold GP#|@ +%n"
                        ).toString(), "Name", "Name"
                    )
                    System.out.format("+-----+-%s-+--------+----------+--------+-%s-+-----+%n", name_dash, name_dash)
                    var index = 0
                    for (pin_info in pins.values) {
                        if (index++ % 2 == 0) {
                            System.out.format(
                                Ansi.ansi().render(
                                    "| @|bold,FG_" + ConsoleUtil.getPinColour(pin_info) + " %3s|@ | @|bold,FG_"
                                            + ConsoleUtil.getPinColour(pin_info) + " %" + max_length
                                            + "s|@ | @|bold %6s|@ | @|bold %2s|@ |"
                                ).toString(),
                                ConsoleUtil.getNotDefined(pin_info.deviceNumber), pin_info.name,
                                ConsoleUtil.getGpiodName(pin_info.chip, pin_info.lineOffset),
                                ConsoleUtil.getNotDefined(pin_info.physicalPin)
                            )
                        } else {
                            System.out.format(
                                Ansi.ansi()
                                    .render(
                                        "| @|bold %-2s|@ | @|bold %-6s|@ | @|bold,FG_" + ConsoleUtil.getPinColour(
                                            pin_info
                                        ) + " %-"
                                                + max_length + "s|@ | @|bold,FG_" + ConsoleUtil.getPinColour(pin_info) + " %-3s|@ |%n"
                                    )
                                    .toString(), ConsoleUtil.getNotDefined(pin_info.physicalPin),
                                ConsoleUtil.getGpiodName(pin_info.chip, pin_info.lineOffset), pin_info.name,
                                ConsoleUtil.getNotDefined(pin_info.deviceNumber)
                            )
                        }
                    }
                    if (index % 2 == 1) {
                        System.out.format("|    |        | %-" + max_length + "s |     |%n", "")
                    }
                    System.out.format("+-----+-%s-+--------+----------+--------+-%s-+-----+%n", name_dash, name_dash)
                    println()
                }
            }
        } finally {
            Diozero.shutdown()
        }
    }
}