package speak;

import org.junit.Test;
import utils.TextToSpeech;

/*
 * Run with  ../gradlew test --tests "SpeakTest"
 */

public class SpeakTest {
    public static void main(String... args) {
        System.out.println("OS is [" + System.getProperty("os.name") + "]");
//	    TextToSpeech.speak("You got a message from 415-745-5209. Do you want to read it?");
        TextToSpeech.speak("Oh hello Pussycat, what's you doing up there?");
    }

    @Test
    public void speakToMe() { // No failure.
//        System.out.println("Speak test!!");
        SpeakTest.main();
    }
}
