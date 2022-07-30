package diozerotests;

import com.diozero.api.RuntimeIOException;

import com.diozero.api.SpiConstants;
import com.diozero.api.SpiDevice;
import com.diozero.util.Diozero;
import com.diozero.util.SleepUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents an MCP3008 analog to digital converter.
 *
 * Requires the following wiring:
 * MCP3008 Vdd-Vref   -> Pi 3V3:  Physical Pin #1
 * MCP3008 Gnd        -> Pi GND:  Physical Pin #6
 * MCP3008 Mosi, Din  -> Pi MOSI: Physical Pin #19
 * MCP3008 Miso, Dout -> Pi MISO: Physical Pin #21
 * MCP3008 Clk        -> Pi SCLK: Physical Pin #23
 * MCP3008 CS         -> Pi CE1:  Physical Pin #26
 *
 * Read MCP3008 Channel 0 (by default, use --channel:X to modify it)
 *
 * TODO See how to change the wiring on the Raspberry Pi...
 */
public class MCP3008 implements AutoCloseable {

    private SpiDevice device;
    private final float vRef;

    /**
     * Creates an instance using the desired chip select and reference voltage.It accepts the diozero default SPI controller and byte ordering.
     * It
     sets an SPI clock frequency to 1.35MHz.
     * @param chipSelect the chip select pin
     * @param vRef the reference voltage
     * @throws java.io.IOException when fails
     */
    public MCP3008(int chipSelect, float vRef) throws IOException {
        this(SpiConstants.DEFAULT_SPI_CONTROLLER, chipSelect, vRef);
    }

    /**
     * Creates an instance using the desired SPI controller, chip select,
     * and reference voltage. It accepts the diozero default byte ordering.
     * It sets an SPI clock frequency to 1.35MHz.
     * @param controller the SPI controller
     * @param chipSelect the chip select pin
     * @param vRef the reference voltage
     * @throws java.io.IOException when fails
     */
    public MCP3008(int controller, int chipSelect, float vRef) throws IOException {
        try {device = SpiDevice.builder(chipSelect)
                .setController(controller)
                .setFrequency(1_350_000).build();
            this.vRef = vRef;
        } catch (RuntimeIOException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * Close the device.
     */
    @Override
    public void close() {
//        System.out.println("close");
        if (device != null) {
            device.close();
            device = null;
        }
    }

    /**
     * Reads a channel and returns the raw value.
     * @param channel channel to read
     * @return raw sample value
     * @throws RuntimeIOException when fails
     */
    public int getRaw(int channel) throws RuntimeIOException {
        // create channel code; assume single-ended
        byte code = (byte) ((channel << 4) | 0x80);
        // first byte has start bit
        // second byte says single-ended, channel
        // third byte for creating third frame
        byte[] tx = {(byte)0x01, code, 0};
        byte[] rx = device.writeAndRead(tx);

        int lsb = rx[2] & 0xff;
        int msb = rx[1] & 0x03;
        int value = (msb << 8) | lsb;
        return value;
    }

    /**
     * Reads a channel and returns its value as a fraction of full scale.
     * @param channel channel to read
     * @return sample value as a fraction of full scale
     * @throws RuntimeIOException when fails
     */
    public float getFSFraction(int channel) throws RuntimeIOException {
        int raw = getRaw(channel);
        float value = raw / (float) 1024; // 1024, really ? not 1023 ?
        return value;
    }

    /**
     * Reads a channel and returns its value as a voltage based on the
     * reference voltage.
     * @param channel channel to read
     * @return sample value as a voltage
     * @throws RuntimeIOException when fails
     */
    public float getVoltage(int channel)  throws RuntimeIOException {
        return (getFSFraction(channel) * vRef);
    }

    private final static String CHANNEL_PREFIX = "--channel:";
    private static int channel = 0;
    private final static String CS_PREFIX = "--cs:";
    private static int cs = SpiConstants.CE1;

    public static void main(String... args) {

        Arrays.asList(args).forEach(arg -> {
            if (arg.startsWith(CHANNEL_PREFIX)) {
                channel = Integer.parseInt(arg.substring(CHANNEL_PREFIX.length()));
                if (channel < 0 || channel > 7) {
                    System.out.println("Invalid channel, must be in [0..7]");
                    System.exit(1);
                }
            } else if (arg.startsWith(CS_PREFIX)) {
                String value = arg.substring(CS_PREFIX.length());
                switch (value) {
                    case "0":
                        cs = SpiConstants.CE0;
                        break;
                    case "1":
                        cs = SpiConstants.CE1;
                        break;
                    default:
                        System.out.printf("Un-managed CS value %s, should be 0 or 1%n", value);
                        break;
                }
            } else {
                System.out.printf("Un-managed CLI parameter %s%n", arg);
            }
        });
        System.out.printf("Will use MCP3008 channel %d%n", channel);
        System.out.printf("Will use CS %d%n", cs);

        AtomicBoolean go = new AtomicBoolean(true);

        final Thread currentThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (currentThread) {
                go.set(false);
                try {
                    currentThread.join();
                    System.out.println("\n... Joining");
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }, "Shutdown Hook"));

        boolean first = true;
        int previousVal = 0;

        try (MCP3008 adc = new MCP3008(cs, 3.3f)) {
            while (go.get()) {
                int value = adc.getRaw(channel);
                if (first || value != previousVal) {
                    System.out.format("C%d = %04d => (%.4f), %.2f FS, %.2fV %n",
                            channel,
                            value,
                            ((float)value / 1023f),
                            adc.getFSFraction(channel),
                            adc.getVoltage(channel));
                    first = false;
                }
                previousVal = value;
                SleepUtil.sleepMillis(100L);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            Diozero.shutdown();
        }
        System.out.println("\nExiting.");
    }
}

