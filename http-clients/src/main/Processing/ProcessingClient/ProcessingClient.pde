import http.client.HTTPClient;
import org.json.JSONObject;


final String restUrl = "http://192.168.42.9:8080/lis3mdl/cache";

public static MagData calculate(double magX, double magY, double magZ) {
    MagData magData = new MagData(); 
    magData.heading = Math.toDegrees(Math.atan2(magY, magX));
    while (magData.heading < 0) {
        magData.heading += 360f;
    }
    magData.pitch = Math.toDegrees(Math.atan2(magY, magZ));
    magData.roll = Math.toDegrees(Math.atan2(magX, magZ));
    return magData;
}

void setup() {
  size(400, 200);
  stroke(255);
  noFill();
  textSize(36);
  surface.setTitle("Mag Data");
  surface.setResizable(true);
}

void draw() {
  background(0);
  fill(255);
  
  try {
    String str = HTTPClient.doGet( restUrl, null);
    if ("true".equals(System.getProperty("verbose"))) {
        System.out.println(str);
    }
    JSONObject magData = new JSONObject(str);
    double magX = magData.getDouble("x");
    double magY = magData.getDouble("y");
    double magZ = magData.getDouble("z");
    MagData data = calculate(magX, magY, magZ);
    text(String.format("Heading: %.02f\u00b0", data.heading), 5, 40);
    text(String.format("Pitch: %.02f\u00b0", data.pitch), 5, 80);
    text(String.format("Roll: %.02f\u00b0", data.roll), 5, 120);
} catch (Exception ex) {
    ex.printStackTrace();
}

  
}
