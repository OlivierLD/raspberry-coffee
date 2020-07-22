package micronaut.sensors;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import sensors.ADCChannel;
import utils.PinUtil;

import javax.annotation.Nullable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller("/sensors")
public class SensorsController {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private final ADCConfiguration adcConfiguration;
    private ADCChannel adcChannel;

    public SensorsController(@Nullable ADCConfiguration adcConfiguration) {
        // For info...
        System.getProperties().forEach((prop, value) -> {
            System.out.println(String.format("Prop %s => %s", prop, value));
        });

        this.adcConfiguration = adcConfiguration;
        if (this.adcConfiguration != null) {

            System.out.println("--------------------------------------");
            System.out.println(this.adcConfiguration.toString());
            System.out.println("--------------------------------------");

            LOGGER.log(Level.ALL, String.format("ADC Config: Channel:%d, MISO:%d, MOSI:%d, CLK:%d, CS:%d",
                this.adcConfiguration.getChannel(),
                this.adcConfiguration.getMiso(),
                this.adcConfiguration.getMosi(),
                this.adcConfiguration.getClk(),
                this.adcConfiguration.getCs()));

            String[] map = new String[4];
            map[0] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByPhysicalNumber(this.adcConfiguration.getClk())).pinNumber()) + ":" + "CLK";
            map[1] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByPhysicalNumber(this.adcConfiguration.getMiso())).pinNumber()) + ":" + "Dout";
            map[2] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByPhysicalNumber(this.adcConfiguration.getMosi())).pinNumber()) + ":" + "Din";
            map[3] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByPhysicalNumber(this.adcConfiguration.getCs())).pinNumber()) + ":" + "CS";

            PinUtil.print(map);

            this.adcChannel = this.adcConfiguration.getADCChannel(
                this.adcConfiguration.getMiso(),
                this.adcConfiguration.getMosi(),
                this.adcConfiguration.getClk(),
                this.adcConfiguration.getCs(),
                this.adcConfiguration.getChannel());
        }
    }

    @Get("/ambient-light")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLuminosity() {
        float volume = 0;
        if (this.adcChannel != null) {
            volume = this.adcChannel.readChannelVolume();
        }
        return String.format("{ \"light\": %05.02f }", volume);
    }
}
