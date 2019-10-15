import processing.serial.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/* Reads RMC & GLL sentences from a Serial GPS, and displays the position on the screen.
 * 
 * Note: This is just a demo, there are many ways to optimize all this!
 *
 * Modify the Serial port name if needed, look for "serialPort = new Serial("...
 *
 * RMC Structure is                                                  12
 *         1      2 3        4 5         6 7     8     9      10    11
 *  $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W,T*6A
 *         |      | |        | |         | |     |     |      |     | |
 *         |      | |        | |         | |     |     |      |     | Type: A=autonomous, D=differential, E=Estimated, N=not valid, S=Simulator
 *         |      | |        | |         | |     |     |      |     Variation sign
 *         |      | |        | |         | |     |     |      Variation value
 *         |      | |        | |         | |     |     Date DDMMYY (see rmc.date.offset property)
 *         |      | |        | |         | |     COG
 *         |      | |        | |         | SOG
 *         |      | |        | |         Longitude Sign
 *         |      | |        | Longitude Value
 *         |      | |        Latitude Sign
 *         |      | Latitude value
 *         |      Active or Void
 *         UTC
 *
 * GLL Structure is
 *         1        2 3         4 5      6 7
 *  $IIGLL,3739.854,N,12222.812,W,014003,A,A*49
 *         |        | |         | |      | |
 *         |        | |         | |      | Type: A=autonomous, D=differential, E=Estimated, N=not valid, S=Simulator
 *         |        | |         | |      A:data valid (Active), V: void
 *         |        | |         | UTC of position
 *         |        | |         Long sign :E/W
 *         |        | Longitude
 *         |        Lat sign :N/S
 *         Latitude
 */

Serial serialPort;

void setup() {
  // List available ports
  printArray(Serial.list());
  // Trying one
  serialPort = new Serial(this, "/dev/tty.usbmodem141401", 4800);

  size(600, 400);
  stroke(255);
  noFill();
  //PFont fontA = loadFont("Courier New");
  //textFont(fontA, 72);
  textSize(72);
}

final boolean DEBUG = false;
final char START_CHARACTER = '$';

StringBuffer sb = new StringBuffer();
char previousChar = ' ';

int calculateCheckSum(String str) {
  int cs = 0;
  char[] ca = str.toCharArray();
  for (int i = 0; i < ca.length; i++) {
    cs ^= ca[i]; // XOR
//  System.out.println("\tCS[" + i + "] (" + ca[i] + "):" + Integer.toHexString(cs));
  }
  return cs;
}

boolean validCheckSum(String data) {
  
  String sentence = data.trim();
  boolean b = false;
  try {
    int starIndex = sentence.indexOf("*");
    if (starIndex < 0) {
      return false;
    }
    String csKey = sentence.substring(starIndex + 1);
    int csk = Integer.parseInt(csKey, 16);
    String str2validate = sentence.substring(1, sentence.indexOf("*"));
    int calcCheckSum = calculateCheckSum(str2validate);
    b = (calcCheckSum == csk);
  } catch (Exception ex) {
    ex.printStackTrace();
  }
  return b;
}

double sexToDec(String degrees, String minutes)
      throws RuntimeException {
  double deg = 0.0D;
  double min = 0.0D;
  double ret = 0.0D;
  try {
    deg = Double.parseDouble(degrees);
    min = Double.parseDouble(minutes);
    min *= (10.0 / 6.0);
    ret = deg + min / 100D;
  } catch (NumberFormatException nfe) {
    throw new RuntimeException("Bad number [" + degrees + "] [" + minutes + "]");
  }
  return ret;
}

enum DATA_TYPE {
  LATITUDE, LONGITUDE
}

String decToSex(double v, DATA_TYPE dataType) {
  String s = "";
  double absVal = Math.abs(v);
  double intValue = Math.floor(absVal);
  double dec = absVal - intValue;
  int i = (int) intValue; //<>//
  dec *= 60D;
  String sign = (v < 0 ? (dataType == DATA_TYPE.LATITUDE ? "S" : "W") : (dataType == DATA_TYPE.LATITUDE ? "N" : "E"));

  s = String.format("%s %d\272%.02f'", sign, i, dec);
  return s;
}

GeoPos position = null;
static String RMC_PATTERN = "^\\$[A-Z]{2}RMC$";
static String GLL_PATTERN = "^\\$[A-Z]{2}GLL$";
static Pattern RMC_COMPILED_PATTERN = Pattern.compile(RMC_PATTERN); //<>//
static Pattern GLL_COMPILED_PATTERN = Pattern.compile(GLL_PATTERN);

void draw() {
  while (serialPort.available() > 0) { //<>//
    int serialByte = serialPort.read();
    char currentChar = (char)serialByte;
    if (DEBUG) {
      println(String.format("%d 0x%02X %s", serialByte, serialByte, currentChar));
    }
    sb.append(currentChar);
    if (currentChar == START_CHARACTER && DEBUG) {
      println("\tStart of sentence detected");
    }
 //<>//
    //println(sb.toString());

    if (currentChar == '\n' && previousChar == '\r') {
      String sentence = sb.toString(); //<>//
      if (DEBUG) {
        println(String.format("Sentence detected: %s", sentence));
      }
      if (validCheckSum(sentence)) {
        String[] data = sentence.substring(0, sentence.indexOf("*")).split(",");
        // TODO Make sure it is an RMC String, data[0] like '$GPRMC' (GP may vary)
        Matcher rmcMatcher = RMC_COMPILED_PATTERN.matcher(data[0]);
        Matcher gllMatcher = GLL_COMPILED_PATTERN.matcher(data[0]);
        if (rmcMatcher.find()) {
          boolean valid = data[2].equals("A");  // Active
          if (valid) {
            if (data[3].length() > 0 && data[5].length() > 0) {
              String deg = data[3].substring(0, 2);
              String min = data[3].substring(2);
              double l = sexToDec(deg, min);
              if ("S".equals(data[4])) {
                l = -l;
              }
              deg = data[5].substring(0, 3);
              min = data[5].substring(3);
              double g = sexToDec(deg, min);
              if ("W".equals(data[6])) {
                g = -g;
              }
              position = new GeoPos()
                .latitude(l)
                .longitude(g);
            }
            background(0);
            fill(255);
            text("Position (RMC)", 5, 72);
            text(decToSex(position.latitude, DATA_TYPE.LATITUDE), 5, 144);
            text(decToSex(position.longitude, DATA_TYPE.LONGITUDE), 5, 216);
          } else {
            println(String.format("%s not active yet.", sentence));
            text("RMC Not Active yet", 5, 72);
          }
        } else if (gllMatcher.find()) {
          boolean valid = data[6].equals("A");  // Active
          if (valid) {
            if (data[1].length() > 0 && data[3].length() > 0) {
              String deg = data[1].substring(0, 2);
              String min = data[1].substring(2);
              double l = sexToDec(deg, min);
              if ("S".equals(data[2])) {
                l = -l;
              }
              deg = data[3].substring(0, 3);
              min = data[3].substring(3);
              double g = sexToDec(deg, min);
              if ("W".equals(data[4])) {
                g = -g;
              }
              position = new GeoPos()
                .latitude(l)
                .longitude(g);
            }
            background(0);
            fill(255);
            text("Position (GLL)", 5, 72);
            text(decToSex(position.latitude, DATA_TYPE.LATITUDE), 5, 144);
            text(decToSex(position.longitude, DATA_TYPE.LONGITUDE), 5, 216);
          } else {
            println(String.format("%s not active yet.", sentence));
            text("GLL Not Active yet", 5, 72);
          }
        } else {
          println(String.format("Dropping [%s], not RMC, not GLL (%s).", data[0], sentence.trim()));
        }
      } else {
        println(String.format("Invalid checksum for %s !", sentence));
      }
      sb.delete(0, sb.length());
    }
    previousChar = currentChar;
  }
}
