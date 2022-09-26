import processing.serial.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.Map;
import java.util.HashMap;
import java.text.NumberFormat;
import java.text.DecimalFormat;


final boolean DEBUG = false;   // set to true for more output
final boolean VERBOSE = false; // set to true for more output

final boolean USE_GLL = true;
final boolean USE_RMC = true;

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

/*
A trick to find the Serial port to use:
With the GPS **not** plugged in, type

 $ ls -lisah /dev/tty* > before.txt

Then connect the GPS on its USB socket

 $ ls -lisah /dev/tty* > after.txt
 $ diff before.txt after.txt
   3a4,5
   > 2589 0 crw-rw-rw-  1 root      wheel   18, 110 Nov 21 07:54 /dev/tty.usbmodem14101
   > 2593 0 crw-rw-rw-  1 root      wheel   18, 112 Nov 21 07:54 /dev/tty.usbmodeme2df64a32
 $
 */

final static String[] MONTHS = {
  "Jan", "Feb", "Mar",
  "Apr", "May", "Jun",
  "Jul", "Aug", "Sep",
  "Oct", "Nov", "Dec"
};
final static NumberFormat SEC_FMT = new DecimalFormat("00.00"); 
final static int FONT_SIZE = 24;

void setup() {
  // List available ports
  println("-- Serial Ports --");
  printArray(Serial.list());
  println("------------------");
  // Trying one
  // String portName = "/dev/tty.usbmodem14242401";
  // String portName = "/dev/tty.usbserial";
  // String portName = "/dev/ttyAMA0";
  String portName = "/dev/ttyACM0";
  serialPort = new Serial(this, portName, 4800);
/* 
  // Moved to settings() (see https://processing.org/reference/size_.html)
  int height = 10 * FONT_SIZE;
  size(700, height);
  */
  stroke(255);
  noFill();
  textSize(FONT_SIZE); 
}

void settings() { // Required in Processing 3.0
  int height = 10 * FONT_SIZE;
  int width = 15 * FONT_SIZE;
  size(width, height);
}

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
} //<>//

double sexToDec(String degrees, String minutes) /* throws RuntimeException */ {
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
  } //<>//
  return ret;
}

enum DataType { //<>//
  LATITUDE, 
  LONGITUDE
}

String decToSex(double v, DataType dataType) {
  String s = "";
  double absVal = Math.abs(v);
  double intValue = Math.floor(absVal);
  double dec = absVal - intValue;
  int i = (int) intValue; //<>//
  dec *= 60D;
  String sign = (v < 0 ? (dataType == DataType.LATITUDE ? "S" : "W") : (dataType == DataType.LATITUDE ? "N" : "E"));

  s = String.format("%s %d\272%.02f'", sign, i, dec); //<>//
  return s;
}

GeoPos position = null;
Map<String, String> sentenceMap = new HashMap<>();

void draw() {
  while (serialPort.available() > 0) {
    int serialByte = serialPort.read();
    char currentChar = (char)serialByte;
    if (VERBOSE) {
      println(String.format("%d 0x%02X %s", serialByte, serialByte, currentChar));
    }
    sb.append(currentChar);
    if (currentChar == START_CHARACTER && VERBOSE) {
      println("\tStart of sentence detected");
    }

    if (currentChar == '\n' && previousChar == '\r') {
      String sentence = sb.toString();
      if (DEBUG) {
        println(String.format("Sentence detected: [%s]", sentence.trim()));
      }
      background(0);
      fill(255);
      textSize(FONT_SIZE);
      
      int yTextPos = 0;
      
      if (sentence.startsWith("$") && validCheckSum(sentence)) {
        String[] data = sentence.substring(0, sentence.indexOf("*")).split(",");
        sentenceMap.put(data[0].substring(3), sentence.trim()); // Only valid sentences go in the map
        if (DEBUG) {
          sentenceMap.forEach((k, v) -> println(String.format("%s => %s", k, v)));
          println("--------------------");
        }
        
        String rmcSentence = sentenceMap.get("RMC");
        if (rmcSentence != null) {
          // println("RMC Sentence:" + rmcSentence);
          data = rmcSentence.substring(0, rmcSentence.indexOf("*")).split(",");
        }
        if (USE_RMC && rmcSentence != null) {
          boolean valid = data[2].equals("A");  // Active
          if (valid) {
            try {
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
            } catch (Exception ex) {
              println("-- err (1) processing " + rmcSentence);
              ex.printStackTrace();
            }
            String dateTime = "";
            if (data[1].length() >= 6 && data[9].length() == 6) { // Date-Time
              try {
                dateTime = "20" + data[9].substring(4) + "-" +                                // Year
                           MONTHS[Integer.parseInt(data[9].substring(2, 4)) - 1] + "-" +      // Month
                           data[9].substring(0, 2) + " " +                                    // Day
                           data[1].substring(0, 2) + ":" +                                    // Hours
                           data[1].substring(2, 4) + ":" +                                    // Minutes
                           SEC_FMT.format(Double.parseDouble(data[1].substring(4))) + " UTC"; // seconds
              } catch (Exception ex) {
                println("-- err (2) processing " + rmcSentence);
                println("Month Val: " + data[9].substring(2, 4));
                println("Sec Val: " + data[1].substring(4));
                println("Sec Double: " + Double.parseDouble(data[1].substring(4)));
                ex.printStackTrace();
              }
            } else {
              println("NO date time: " + data[1] + " " + data[9]);
            }
            textSize(FONT_SIZE);
            yTextPos += FONT_SIZE;
            text("Position (RMC)", 5, yTextPos);
            yTextPos += FONT_SIZE;
            text(decToSex(position.latitude, DataType.LATITUDE), 5, yTextPos);
            yTextPos += FONT_SIZE;
            text(decToSex(position.longitude, DataType.LONGITUDE), 5, yTextPos);
            yTextPos += FONT_SIZE;
            text("Date-Time (RMC)", 5, yTextPos);
            yTextPos += FONT_SIZE;
            text(dateTime, 5, yTextPos);
          } else {
            println(String.format("%s not active yet.", sentence.trim()));
            yTextPos += FONT_SIZE;
            text("RMC Not Active yet", 5, yTextPos);
            textSize(FONT_SIZE);
          }
        } 
        String gllSentence = sentenceMap.get("GLL");
        if (gllSentence != null) {
          data = gllSentence.substring(0, gllSentence.indexOf("*")).split(",");
        }
        if (USE_GLL && gllSentence != null) {
          boolean valid = data[6].equals("A");  // Active
          if (valid) {
            try {
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
            } catch (Exception ex) {
              println("-- err (1) processing " + gllSentence);
              ex.printStackTrace();
            }
            if (yTextPos > 0) {
              yTextPos += FONT_SIZE;
            }
            yTextPos += FONT_SIZE;
            text("Position (GLL)", 5, yTextPos);
            yTextPos += FONT_SIZE;
            text(decToSex(position.latitude, DataType.LATITUDE), 5, yTextPos);
            yTextPos += FONT_SIZE;
            text(decToSex(position.longitude, DataType.LONGITUDE), 5, yTextPos);
          } else {
            println(String.format("%s not active yet.", sentence.trim()));
            yTextPos += FONT_SIZE;
            text("GLL Not Active yet", 5, yTextPos);
            textSize(FONT_SIZE);
          }
        }
      } else {
        println(String.format("Invalid checksum for [%s] !", sentence.trim()));
      }
      sb.delete(0, sb.length());
    }
    previousChar = currentChar;
  }
}
