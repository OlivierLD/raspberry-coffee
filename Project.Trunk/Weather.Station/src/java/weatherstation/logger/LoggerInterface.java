package weatherstation.logger;

import org.json.JSONObject;

public interface LoggerInterface {
	void pushMessage(JSONObject json) throws Exception;
	void close();
}
