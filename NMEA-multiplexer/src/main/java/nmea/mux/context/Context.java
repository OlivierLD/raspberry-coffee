package nmea.mux.context;

//import http.HTTPServer;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a singleton for the whole mux application
 */
public class Context {
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	static {
		LOGGER.setLevel(Level.INFO);
	}

	private long startTime = 0L;
	private long managedBytes = 0L;

	private long nbMessReceived = 0L;

	private String lastDataSentence = "";
	private long lastSentenceTimestamp = 0L;

	private static Context instance;

	public synchronized static Context getInstance() {
		if (instance == null) {
			instance = new Context();
		}
		return instance;
	}

	/**
	 * Those topic listeners can be used like regular event listeners.
	 * They've be designed to be used in conjunction with the POST /events/{topic} service, though.
	 * <br/>
	 * See {@_link nmea.mux.RESTImplementation#broadcastOnTopic(HTTPServer.Request)} for more details about that.
	 */
	private List<TopicListener> topicListeners = new ArrayList<>();
	public void addTopicListener(TopicListener topicListener) {
		synchronized (this.topicListeners) {
			this.topicListeners.add(topicListener);
		}
	}
	public void removeTopicListener(TopicListener topicListener) {
		if (this.topicListeners.contains(topicListener)) {
			synchronized (this.topicListeners) {
				this.topicListeners.remove(topicListener);
			}
		}
	}

	/**
	 *
	 * @param topic A RegEx to match the topic of the payload. The payload will be sent only to those who subscribed to a topic matching the regex.
	 * @param payload Usually a Map&lt;String, Object&gt;, representing the payload json object. Can be null.
	 */
	public void broadcastOnTopic(String topic, Object payload) {
		Pattern pattern = Pattern.compile(topic);
		this.topicListeners.stream()
				.filter(tl -> {
					Matcher matcher = pattern.matcher(tl.getSubscribedTopic());
//		    	System.out.println(
//		    			String.format("[%s] %s [%s]",
//						    tl.getSubscribedTopic(),
//						    (matcher.matches() ? "matches" : "does not match"),
//						    topic));
					return matcher.matches();
				})
				.forEach(tl -> {
					tl.topicBroadcast(topic, payload);
				});
	}

	public Logger getLogger() {
		return this.LOGGER;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getManagedBytes() {
		return managedBytes;
	}

	public void addManagedBytes(long managedBytes) {
		this.managedBytes += managedBytes;
	}

	public void setLastDataSentence(String sentence) {
		try {
			this.nbMessReceived++;
		} catch (Exception ex) { // Overflow? TODO trap it for real
			ex.printStackTrace();
		}
		this.lastDataSentence = sentence;
		this.lastSentenceTimestamp = System.currentTimeMillis();
	}
	public StringAndTimeStamp getLastDataSentence() {
		return new StringAndTimeStamp(this.lastDataSentence, this.lastSentenceTimestamp);
	}
	public long getNbMessReceived() {
		return this.nbMessReceived;
	}

	public static class StringAndTimeStamp {
		String str;
		long timestamp;
		public StringAndTimeStamp(String str, long ts) {
			this.str = str;
			this.timestamp = ts;
		}
		public String getString() { return this.str; }
		public long getTimestamp() { return this.timestamp; }
	}

	public static abstract class TopicListener implements EventListener {
		private String subscribedTopic;
		public TopicListener(String topic) {
			this.subscribedTopic = topic;
		}
		public String getSubscribedTopic() {
			return this.subscribedTopic;
		}

		public abstract void topicBroadcast(String topic, Object payload);
	}
}
