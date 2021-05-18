package oliv.oda;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class OlivODANL2RulesClient {

    public OlivODANL2RulesClient() {
    }

    private final static String NL2RULES_SERVICE_RESOURCE = "http://100.102.86.190:8500/nl2rule/infer";
    private static ObjectMapper mapper = new ObjectMapper();

    public String invokeService(Client client,
                                String serviceEndPoint,
                                String utterance) throws Exception {
        String result = "";
        // Two - Invoke the Decision Service
        String serviceURL = serviceEndPoint;
        String nl2RulesPayload = String.format("{\"utterance\": \"%s\"}", utterance);
        try {
            System.out.printf("GETTING to [%s]\n", serviceURL);
            Response httpResponse = client.target(serviceURL)
                    .request(MediaType.WILDCARD)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .post(Entity.json(nl2RulesPayload));
            if (httpResponse.getStatus() != Response.Status.OK.getStatusCode() &&
                    httpResponse.getStatus() != Response.Status.ACCEPTED.getStatusCode()) {
                String errorResponse = httpResponse.readEntity(String.class);
                throw new Exception("Error sending request: status=" + httpResponse.getStatus() + "; response=" + errorResponse);
            }
            // Get the response
            String responseStr = httpResponse.readEntity(String.class);

            result = responseStr;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static void main(String... args) throws Exception {
        Client client = ClientBuilder.newClient();

        OlivODANL2RulesClient decisionServiceInvoker = new OlivODANL2RulesClient();

        String utterance = "";
        String result = decisionServiceInvoker.invokeService(
                client,
                NL2RULES_SERVICE_RESOURCE,
                utterance);

        System.out.printf("----------\nRESULT: %s\n----------\n", result);
    }
}
