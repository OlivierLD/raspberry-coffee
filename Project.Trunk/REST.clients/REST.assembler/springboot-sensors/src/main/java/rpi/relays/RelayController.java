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

	private RelayManager physicalRelayManager;

	public RelayController() {
		super();
		System.out.println("-----------------------------------");
		System.out.println(String.format(">>> Instantiating the %s", this.getClass().getName()));
		System.out.println("-----------------------------------");
		// Create the relay interface here
		this.physicalRelayManager = new RelayManager(System.getProperty("relay.map", "1:11"));
	}

	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot Relay Manager!";
	}

	@RequestMapping(value = "/status/{relay-id}", method = RequestMethod.GET)
	public boolean getRelayStatus(@PathVariable("relay-id") int relayId) {

		return this.physicalRelayManager.get(relayId);

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

		if (this.physicalRelayManager != null) {
			this.physicalRelayManager.set(relayId, (relayStatus.status ? "on" : "off")); // TODO an enum ?
		}

		return relayStatus;
	}

}
