package obd.simulation;

import java.io.BufferedReader;
import java.io.FileReader;

public class FirstTest {

	public static void main(String... args) {
		try {
			BufferedReader br = new BufferedReader(new FileReader("CBC40C04_00000086_00000002.mf4"));
			String line = "";
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
