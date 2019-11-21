package processing.sample;

import processing.core.PApplet;

import java.util.Arrays;
import java.util.List;

public class FullSketch extends PApplet {
	public void setup() {
		stroke(255);
		noFill();
		textSize(72);
		// Here you can use Java 8 syntax
		List<String> testListJava8 = Arrays.asList(new String[] {"One", "Two", "Three"});
		testListJava8.stream().forEach(System.out::println);
	}

	public void draw() {
		background(0);
		fill(255);
		int value = (int)Math.floor(1023 * Math.random());  // Simulation
		text(String.format("%04d", value), 10, 100);
	}

	public void settings() {  size(600, 400); }

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "processing.sample.FullSketch" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}
}
