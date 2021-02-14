package util;

public class TextToSpeechTest {

    public static void main(String... args) {
        System.out.println("OS is [" + System.getProperty("os.name") + "]");
//	speak("You got a message from 415-745-5209. Do you wan to read it?");
        TextToSpeech.speak("Oh hello Pussycat, whats you doing up there?");
    }

}
