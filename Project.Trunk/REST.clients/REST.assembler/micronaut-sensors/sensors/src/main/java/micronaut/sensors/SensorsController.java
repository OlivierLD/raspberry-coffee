package micronaut.sensors;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import rpi.sensors.ADCChannel;

import javax.annotation.Nullable;

@Controller("/ambient-light")
public class SensorsController {

    private final ADCConfiguration adcConfiguration;
    private ADCChannel adcChannel;

    public SensorsController(@Nullable ADCConfiguration adcConfiguration) {
        // For info...
        System.getProperties().forEach((prop, value) -> {
            System.out.println(String.format("Prop %s => %s", prop, value));
        });

        this.adcConfiguration = adcConfiguration;
        if (this.adcConfiguration != null) {
            System.out.println(String.format("ADC Config: Channel:%d, MISO:%d, MOSI:%d, CLK:%d, CS:%d",
                this.adcConfiguration.getChannel(),
                this.adcConfiguration.getMiso(),
                this.adcConfiguration.getMosi(),
                this.adcConfiguration.getClk(),
                this.adcConfiguration.getCs()));
            this.adcChannel = new ADCChannel(
                this.adcConfiguration.getMiso(),
                this.adcConfiguration.getMosi(),
                this.adcConfiguration.getClk(),
                this.adcConfiguration.getCs(),
                this.adcConfiguration.getChannel());
        }
    }

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public String getLuminosity() {
        float volume = 0;
        if (this.adcChannel != null) {
            volume = this.adcChannel.readChannelVolume();
        }
        return String.format("{ \"light\": %05.02f }", volume);
    }
}
