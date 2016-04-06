import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
// import javax.xml.ws.Response

Client cl = ClientBuilder.newClient()
WebTarget target = cl.target("https://io.adafruit.com/api/feeds/onoff")
def resp = target.request()
                 .header("X-AIO-Key", "54c2767878ca793f2e3cae1c45d62aa7ae9f8056")
                 .header("Accept",    "application/json")
                 .get(String.class) // Response.class)
println "resp is a ${resp.getClass().getName()}:"
println resp
cl.close()
