package utils;

import java.io.*;

public class RemoveBlankLines {
  public static void main(String... args) throws FileNotFoundException, IOException
  {
    if (args.length != 1) {
      System.out.println("Yo!"); // Argh!
    } else {
      String fName = args[0];
      System.out.println(String.format("Reworking %s into new.txt.", fName));
      BufferedReader br = new BufferedReader(new FileReader(fName));
      BufferedWriter bw = new BufferedWriter(new FileWriter("new.txt"));
      String line = "";
      boolean keepGoing = true;
      while (keepGoing) {
        line = br.readLine();
        if (line == null) {
          keepGoing = false;
        } else {
     //    System.out.println(String.format("Length: %d", line.length()));
          if (line.length() > 0) {
            bw.write(line + "\n");
          }
        }
      }
      br.close();
      bw.close();
    }
  }
}
