package nmea.computers;

import util.TextToSpeech;

import java.util.function.Consumer;

/**
 * This is just an example for AISManager collision callback.
 * See in nmea.computers.AISManager and ais.mgr.properties
 *
 * This one is talking the message.
 *
 * The same Consumer could be used to turn on a light or a buzzer, even if the accept String parameter is not used.
 */
public class SpeakingCallback implements Consumer<String> {

    @Override
    public void accept(String s) {
        TextToSpeech.speak(s);
    }
}
