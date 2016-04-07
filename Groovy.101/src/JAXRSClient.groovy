import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
// import javax.xml.ws.Response

def key = "54c2767878ca793f2e3cae1c45d62aa7ae9f8056"
for (a in this.args) {
    println("Received arg: " + a)
    key = a
}
Client cl = ClientBuilder.newClient()
WebTarget target = cl.target("https://io.adafruit.com/api/feeds/onoff")
def resp = target.request()
                 .header("X-AIO-Key", key)
                 .header("Accept",    "application/json")
                 .get(String.class) // Response.class)
println "resp is a ${resp.getClass().getName()}:"
println resp
cl.close()
