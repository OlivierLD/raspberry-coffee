import java.util.Arrays;
import java.util.List;

final int WIDTH = 600;
final int HEIGHT = 400;
final int TEXT_SIZE = 144;

void setup() {
    stroke(255);
    noFill();
    textSize(TEXT_SIZE);
    // Here you can use Java 8 syntax
    List<String> testListJava8 = Arrays.asList(new String[] {"One...", "Two...", "Three!"});
    testListJava8.stream().forEach(System.out::println);
    frameRate(5); // Default ~60. Number of 'draw' per second.
}

void draw() {
    background(0);
    fill(255);
    int value = (int)Math.floor(65_535 * Math.random());  // Simulation
    int x = 120;
    int y = (230); // (HEIGHT / 2) + (TEXT_SIZE / 2);
    text(String.format("%05d", value), x, y);
    println(frameRate);
}

void settings() {  
    size(WIDTH, HEIGHT); 
}
