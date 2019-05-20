package rpi.sensors;

import com.pi4j.io.gpio.Pin;
import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import utils.PinUtil;

import java.util.Map;

@RestController
@RequestMapping("/light")
public class SensorsController {

	private ADCChannel physicalADCChannel;

	public SensorsController() {
		super();
		System.out.println("-----------------------------------");
		System.out.println(String.format(">>> Instantiating the %s", this.getClass().getName()));
		System.out.println("-----------------------------------");
		// Create the mcp3008 interface here
		int miso = 0, mosi = 10, clk = 11, cs = 8, channel = 0;

		String misoStr = System.getProperty("miso.pin", String.valueOf(miso));
		String mosiStr = System.getProperty("mosi.pin", String.valueOf(mosi));
		String clkStr  = System.getProperty("clk.pin", String.valueOf(clk));
		String csStr   = System.getProperty("cs.pin", String.valueOf(cs));

		String adcChannelStr   = System.getProperty("adc.channel", String.valueOf(channel));

		try {
			miso = Integer.parseInt(misoStr);
			mosi = Integer.parseInt(mosiStr);
			clk = Integer.parseInt(clkStr);
			cs = Integer.parseInt(csStr);
			channel = Integer.parseInt(adcChannelStr);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		if ("true".equals(System.getProperty("server.verbose", "false"))) {
			System.out.println(String.format("MISO:%d MOSI:%d CLK:%d CS:%d, Channel:%d", miso, mosi, clk, cs, channel));
		}
		this.physicalADCChannel = new ADCChannel(miso, mosi, clk, cs, channel);
	}

	@RequestMapping("/") // No method: means they all work!
	public String index() {
		return "Greetings from Spring Boot Light sensor!";
	}

	public static class AmbientLight {
		float percent;

		public float getPercent() {
			return percent;
		}

		public void setPercent(float percent) {
			this.percent = percent;
		}
	}

	@RequestMapping(value = "/ambient", method = RequestMethod.GET)
	public AmbientLight getLight() {

		AmbientLight light = new AmbientLight();
		light.setPercent(this.physicalADCChannel.readChannelVolume());

		return light;
	}

}
