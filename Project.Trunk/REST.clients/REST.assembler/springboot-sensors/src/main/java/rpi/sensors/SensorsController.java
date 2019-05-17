package rpi.sensors;

import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/light")
public class SensorsController {

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
		// TODO Implement
		light.setPercent((float)(100f * Math.random()));
		return light;
	}

}
