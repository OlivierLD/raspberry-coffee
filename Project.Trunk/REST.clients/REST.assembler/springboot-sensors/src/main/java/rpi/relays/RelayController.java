package rpi.relays;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relay")
public class RelayController {

	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot Relay Manager!";
	}

	@RequestMapping(value = "/status/{relay-id}", method = RequestMethod.GET)
	public boolean getRelayStatus(@PathVariable("relay-id") int relayId) {
		// TODO Implement
		return (System.currentTimeMillis() % 2 == 0);
	}

	// Needs to be a JavaBean to be returned as JSON
	public static class RelayStatus {
		private boolean status;

		public boolean isStatus() {
			return status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}
	}

	@RequestMapping(
			value = "/status/{relay-id}",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
//	@ResponseBody
	public RelayStatus setRelayStatus(@PathVariable("relay-id") int relayId,  @RequestBody RelayStatus relayStatus) {

		// TODO Implement
		System.out.println(String.format("Setting relay status of %d to %s", relayId, relayStatus.isStatus() ? "true" : "false"));

		return relayStatus;
	}

}
