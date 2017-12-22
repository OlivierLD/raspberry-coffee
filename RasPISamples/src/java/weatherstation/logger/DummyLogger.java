package weatherstation.logger;

import org.json.JSONObject;

public class DummyLogger implements LoggerInterface {
	@Override
	public void pushMessage(JSONObject json)
			throws Exception {
		System.out.println(">>> Logging:\n" + json.toString(2));
	}
}
