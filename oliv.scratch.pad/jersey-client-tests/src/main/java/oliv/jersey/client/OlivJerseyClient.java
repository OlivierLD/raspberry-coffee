package oliv.jersey.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class OlivJerseyClient {

	public static class AgentStatus {
		private boolean status = false;
		private String message;

		public AgentStatus() {
		}

		public boolean isStatus() {
			return status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public AgentStatus status(boolean status) {
			this.status = status;
			return this;
		}
		public AgentStatus message(String message) {
			this.message = message;
			return this;
		}

		@Override
		public String toString() {
			return String.format("Status: %b, Message: %s", this.status, this.message);
		}
	}

	public static class Config {
		private String oicURL;
		private String agentGroup;
		private String oicUser;
		private String oicPassword;

		public Config() {
		}

		public String getOicURL() {
			return oicURL;
		}

		public void setOicURL(String oicURL) {
			this.oicURL = oicURL;
		}

		public String getAgentGroup() {
			return agentGroup;
		}

		public void setAgentGroup(String agentGroup) {
			this.agentGroup = agentGroup;
		}

		public String getOicUser() {
			return oicUser;
		}

		public void setOicUser(String oicUser) {
			this.oicUser = oicUser;
		}

		public String getOicPassword() {
			return oicPassword;
		}

		public void setOicPassword(String oicPassword) {
			this.oicPassword = oicPassword;
		}

		public Config oicURL(String oicURL) {
			this.oicURL = oicURL;
			return this;
		}
		public Config agentGroup(String agentGroup) {
			this.agentGroup = agentGroup;
			return this;
		}
		public Config oicUser(String oicUser) {
			this.oicUser = oicUser;
			return this;
		}
		public Config oicPassword(String oicPassword) {
			this.oicPassword = oicPassword;
			return this;
		}

		public String toString() {
			return String.format("%s, %s, %s, %$s", oicURL, agentGroup, oicUser, oicPassword);
		}
	}

	private final static String AGENT_RESOURCE = "http://localhost:9990/agent";

	public static void main(String... args) throws Exception {
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(AGENT_RESOURCE);
		WebTarget statusTarget = webTarget.path("/status");
		Invocation.Builder invocationBuilder = statusTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.get();
		int status = response.getStatus();
		System.out.println(String.format("Status: %d", status));
		if (status == 200) {
			String entity = response.readEntity(String.class);
			System.out.println(String.format("Response Payload: %s", entity));
		} else {
			System.out.println("There was a problem?");
		}
		System.out.println("Done with status.");

		Config config = new Config()
				.oicUser("akeu")
				.oicPassword("coucou")
				.oicURL("http://akeu/coucou")
				.agentGroup("GROUP-1");
		Response postResponse = client
				.target(AGENT_RESOURCE)
				.path("/start")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(config, MediaType.APPLICATION_JSON));
		System.out.println(String.format("Post status: %d", postResponse.getStatus()));
		AgentStatus agentStatus = postResponse.readEntity(AgentStatus.class);
		System.out.printf("Post response payload: %s\n", agentStatus.toString());

	}

}
