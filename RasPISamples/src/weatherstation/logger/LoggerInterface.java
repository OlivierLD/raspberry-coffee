package weatherstation.logger;

import org.json.JSONObject;

public interface LoggerInterface
{
  public void pushMessage(JSONObject json) throws Exception;
}
