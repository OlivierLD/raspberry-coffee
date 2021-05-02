package oliv.oda;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class OlivODADecisionTableClient {

    private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    static {
        DURATION_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
    }

    /* OAuth query:
     * curl -L -X POST 'https://idcs-8ec236754c304a6d9ff0afae76347b8e.identity.preprod.oraclecloud.com/oauth2/v1/token'
     *      -H 'Authorization: Basic aWRjcy1vZGEtZDk5MmNmYmFiYzE3NDRiYWFiNzY2ZmI3NDY0YjkyNGMtczBfQVBQSUQ6MDZiM2Y5YjAtMjI3YS00ODlhLWJlZjctZDBiMjk3NmMzYzBi'
     *      -H 'Content-Type: application/x-www-form-urlencoded'
     *      --data-raw 'scope=https://idcs-oda-d992cfbabc1744baab766fb7464b924c-s0.data.digitalassistant.oci.oc-test.com/process&grant_type=password&username=oda-ServiceAdministrator&password=We1come12345*&expiry=3600'
     */

    public OlivODADecisionTableClient() {
    }

    public String updateDecisionService(Client client,
                                        String oAuthUserName,
                                        String oAuthPassword,
                                        String appName,
                                        String appVersion,
                                        String decisionTableName,
                                        String decisionServiceEndPoint) throws Exception {
        String result = "";

        // One - OAuth
        String oAuthEndPointURL = "https://idcs-8ec236754c304a6d9ff0afae76347b8e.identity.preprod.oraclecloud.com/oauth2/v1/token";
        String oAuthBasicAuthHeader = "Basic aWRjcy1vZGEtZDk5MmNmYmFiYzE3NDRiYWFiNzY2ZmI3NDY0YjkyNGMtczBfQVBQSUQ6MDZiM2Y5YjAtMjI3YS00ODlhLWJlZjctZDBiMjk3NmMzYzBi";
//		String oAuthContentTypeHeader = "Content-Type: application/x-www-form-urlencoded";
//		String oAuthPayloadBody = String.format("scope=https://%s/process&grant_type=password&username=%s&password=%s&expiry=3600",
//				decisionServiceEndPoint,
//				oAuthUserName,
//				oAuthPassword);

        final Form form = new Form();
//		form.param("scope", String.format("https://%s/process", decisionServiceEndPoint));
        form.param("scope", "https://{END_POINT}/process".replace("{END_POINT}", decisionServiceEndPoint));
        form.param("grant_type", "password");
        form.param("username", oAuthUserName);
        form.param("password", oAuthPassword);
        form.param("expiry", "3600");

        Response oauthResponse = client.target(oAuthEndPointURL)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, oAuthBasicAuthHeader)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        if (oauthResponse.getStatus() != Response.Status.OK.getStatusCode() &&
                oauthResponse.getStatus() != Response.Status.ACCEPTED.getStatusCode()) {
            String errorResponse = oauthResponse.readEntity(String.class);
            throw new Exception("Error sending request: status=" + oauthResponse.getStatus() + "; response=" + errorResponse);
        }
        // Get the access_token here
        String oAuthResponse = oauthResponse.readEntity(String.class);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> oAuthMap = mapper.readValue(oAuthResponse, Map.class);
        // Extract token
        String accessToken = // "eyJ4NXQjUzI1NiI6Im9yelRYZk51c25TTXEtQmhaYzdqbjVxY29ON3FLRHRhUGpaeUQzSHFZN3ciLCJ4NXQiOiI4V2U4ek92WW5aMlFoSEtCSk1SeEtZY21YaVUiLCJraWQiOiJTSUdOSU5HX0tFWSIsImFsZyI6IlJTMjU2In0.eyJ1c2VyX3R6IjoiQW1lcmljYVwvQ2hpY2FnbyIsInN1YiI6Im9kYS1TZXJ2aWNlQWRtaW5pc3RyYXRvciIsInVzZXJfbG9jYWxlIjoiZW4iLCJ1c2VyLnRlbmFudC5uYW1lIjoiaWRjcy04ZWMyMzY3NTRjMzA0YTZkOWZmMGFmYWU3NjM0N2I4ZSIsImlzcyI6Imh0dHBzOlwvXC9pZGVudGl0eS5vcmFjbGVjbG91ZC5jb21cLyIsInVzZXJfdGVuYW50bmFtZSI6ImlkY3MtOGVjMjM2NzU0YzMwNGE2ZDlmZjBhZmFlNzYzNDdiOGUiLCJjbGllbnRfaWQiOiJpZGNzLW9kYS1kOTkyY2ZiYWJjMTc0NGJhYWI3NjZmYjc0NjRiOTI0Yy1zMF9BUFBJRCIsInN1Yl90eXBlIjoidXNlciIsInNjb3BlIjoiXC9wcm9jZXNzIiwiY2xpZW50X3RlbmFudG5hbWUiOiJpZGNzLThlYzIzNjc1NGMzMDRhNmQ5ZmYwYWZhZTc2MzQ3YjhlIiwidXNlcl9sYW5nIjoiZW4iLCJleHAiOjE2MTg4NTEyMDYsImlhdCI6MTYxODg0NzYwNiwiY2xpZW50X2d1aWQiOiIzZWY5YTQyNzdkMzE0NmY1YjU4OThkMTQ4ZmJhZTVlNCIsImNsaWVudF9uYW1lIjoiaWRjcy1vZGEtZDk5MmNmYmFiYzE3NDRiYWFiNzY2ZmI3NDY0YjkyNGMtczAiLCJ0ZW5hbnQiOiJpZGNzLThlYzIzNjc1NGMzMDRhNmQ5ZmYwYWZhZTc2MzQ3YjhlIiwianRpIjoiMTFlYmExMjc2MGRkMTRkZGIwOTcyYmE5MTNjYTc4MmEiLCJndHAiOiJybyIsInVzZXJfZGlzcGxheW5hbWUiOiJBIiwic3ViX21hcHBpbmdhdHRyIjoidXNlck5hbWUiLCJwcmltVGVuYW50Ijp0cnVlLCJ0b2tfdHlwZSI6IkFUIiwiY2FfZ3VpZCI6ImNhY2N0LWViZjczMTc2MWVmYzQ1M2M5MWFiZTFkZThmZGJlOTEwIiwiYXVkIjoiaHR0cHM6XC9cL2lkY3Mtb2RhLWQ5OTJjZmJhYmMxNzQ0YmFhYjc2NmZiNzQ2NGI5MjRjLXMwLmRhdGEuZGlnaXRhbGFzc2lzdGFudC5vY2kub2MtdGVzdC5jb20iLCJ1c2VyX2lkIjoiZmFjYzdhYmZjNTY1NGY2MjhlNTk3NWM5YTk4NjI0ZWQiLCJ0ZW5hbnRfaXNzIjoiaHR0cHM6XC9cL2lkY3MtOGVjMjM2NzU0YzMwNGE2ZDlmZjBhZmFlNzYzNDdiOGUuaWRlbnRpdHkucHJlcHJvZC5vcmFjbGVjbG91ZC5jb20iLCJyZXNvdXJjZV9hcHBfaWQiOiIzZWY5YTQyNzdkMzE0NmY1YjU4OThkMTQ4ZmJhZTVlNCJ9.YW75mYsIb4H4sqsxq76yT9zhfpz1NRyWUSVovdccfWY-ALWBYHWTFtM_n-CU78F2bYUxsXSFLmyTV8qh29fWpE1S4XxPXOyXfPuXXp-71FUfmD2MnAtWtATsmbew6Ep-B14-YqBn1bzO9rmdSHsXrAPCk573PR75ofVooN-FGmlLHX69vaZxKc2f7LGacs1FsDSlYfrc0ZFM5nhRMNu42Vsdu90HpVjz7mYiXxejUTZxN3rRniDL6VL7DZrkcEHunfsuTzq0hukQD4lCcj1w8PLfQkwWcdhTAsMDkgR-RfliK3UW9qo2G4yBRPbKRE3mTCtLw0n5ATNcvID6c20rLg";
                (String) oAuthMap.get("access_token");

        {
            // Two - Invoke the Decision Service
            String decisionServiceURL = "https://{END_POINT}/decision/api/v1/decision-models/{APP_NAME}/versions/{VERSION}/latest/definition/decisions/{DECISION_TABLE}"
                    .replace("{END_POINT}", decisionServiceEndPoint)
                    .replace("{APP_NAME}", appName)
                    .replace("{VERSION}", appVersion)
                    .replace("{DECISION_TABLE}", decisionTableName);

            try {
                System.out.printf("GETTING to [%s]\n", decisionServiceURL);
                Response httpResponse = client.target(decisionServiceURL)
                        .request(MediaType.WILDCARD)
                        .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .get();
                if (httpResponse.getStatus() != Response.Status.OK.getStatusCode() &&
                        httpResponse.getStatus() != Response.Status.ACCEPTED.getStatusCode()) {
                    String errorResponse = httpResponse.readEntity(String.class);
                    throw new Exception("Error sending request: status=" + httpResponse.getStatus() + "; response=" + errorResponse);
                }
                // Get the response
                String responseStr = httpResponse.readEntity(String.class);

                if (true) {
                    Map<String, Object> decisionMap = mapper.readValue(responseStr, Map.class);
                    String interpretationTime = (String)decisionMap.get("interpretationTime");
                    String now = DURATION_FMT.format(new Date());
                    System.out.printf("interpretationTime:%s, now:%s\n", interpretationTime, now);

                    List<Map> rules = (List)((Map)decisionMap.get("logic")).get("rules");
                    rules.forEach(rule -> {
                        List<Map> inputEntries = (List)rule.get("inputEntries");
                        inputEntries.forEach(ie -> {
                            Map<String, Object> range = (Map)ie.get("range");
                            if (range != null) {
                                String endpoint1 = (String)range.get("endpoint1");
                                if (endpoint1 != null) {
                                    System.out.printf("EndPoint1: %s, %s\n", endpoint1, range.get("operation1"));
                                } else {
                                    System.out.println("No Endpoint1");
                                }
                            } else {
                                System.out.println("No Range");
                            }
                        });
                    });
                    // Modified map back to string
                    String jsonInString = mapper.writeValueAsString(decisionMap);
                    System.out.println(jsonInString);

                    // Now PUT
                    System.out.printf("PUTTING to [%s]\n", decisionServiceURL);
                    String original    = "100";
                    String replaceWith = "150";
                    String newDefinition = responseStr.replace(original, replaceWith); // That's a bit tough. It's a POC.

//                    System.out.println(responseStr.replace("150", "250"));

                    Response putResponse = client.target(decisionServiceURL)
                            .request(MediaType.WILDCARD)
                            .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken))
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .put(Entity.entity(newDefinition, MediaType.APPLICATION_JSON));
                    if (putResponse.getStatus() != Response.Status.OK.getStatusCode() &&
                            putResponse.getStatus() != Response.Status.ACCEPTED.getStatusCode() &&
                            putResponse.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                        String errorResponse = putResponse.readEntity(String.class);
                        throw new Exception("Error sending request: status=" + putResponse.getStatus() + "; response=" + errorResponse);
                    }
                    result = "OK";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static void main(String... args) throws Exception {
        Client client = ClientBuilder.newClient();

        String oAuthUserName = "oda-ServiceAdministrator"; // DEFAULT_OAUTH_USER_NAME;
        String oAuthPassword = "We1come12345*";            // DEFAULT_OAUTH_PASSWORD;

        String appName = "OlivTestApp";
        String appVersion = "1.0";
        String decisionTable = "BigEnough";
        String decisionServiceEndPoint = "idcs-oda-d992cfbabc1744baab766fb7464b924c-s0.data.digitalassistant.oci.oc-test.com";

        OlivODADecisionTableClient decisionServiceInvoker = new OlivODADecisionTableClient();

        String result = decisionServiceInvoker.updateDecisionService(
                client,
                oAuthUserName,
                oAuthPassword,
                appName,
                appVersion,
                decisionTable,
                decisionServiceEndPoint);

        System.out.printf("----------\nRESULT: %s\n----------\n", result);
    }
}
