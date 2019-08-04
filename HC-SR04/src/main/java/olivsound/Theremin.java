package olivsound;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import rangesensor.StandaloneHC_SR04;
import rangesensor.JNI_HC_SR04;

/**
 * Uses WiringPI, bridged with javah.
 * The pure Java implementation (see {@link StandaloneHC_SR04}.java) seems to have problems
 * with the nano seconds required here.
 */
public class Theremin {

	public native void init();

	/*
	Default pins, in the C code:

#define GPIO23     4
#define GPIO24     5

using namespace std;

static int trigger = GPIO23;
static int echo    = GPIO24;
	 */
	public native void init(int trigPin, int echoPin); // Uses the WiringPi numbers. See default above.

	public native double readRange();

	public static void main(String... args) throws Exception {

		System.out.println("Theremin, initializing the sound part...");

		Synthesizer synth = MidiSystem.getSynthesizer();
		synth.open();

		final MidiChannel[] mc = synth.getChannels();
		Instrument[] instr = synth.getDefaultSoundbank().getInstruments();

		synth.loadInstrument(instr[90]);
		// Scale: 0 to 120
		for (int i = 20; i < 120; i++) {
			mc[5].noteOn(i, 600);
			Thread.sleep(10);
			mc[5].noteOff(i);
		}
		for (int i = 119; i >= 20; i--) {
			mc[5].noteOn(i, 600);
			Thread.sleep(20);
			mc[5].noteOff(i);
		}

		System.out.println("Sound initialized, now initializing the range sensor");
		JNI_HC_SR04 jni_hc_sr04 = new JNI_HC_SR04();
		jni_hc_sr04.init(); // With default prms. See above.
		System.out.println("Initialized. Get closer than 5cm to stop.");
		boolean go = true;
		while (go) {
			double range = jni_hc_sr04.readRange(); // in meters.
			System.out.println(String.format("Distance is %.2f cm.", (range * 100)));
			go = (range * 100 > 5); // Stops when range is less than 5 cm.
			if (go) {
				int note = (int) (Math.round(((range * 100) / 55) * 100)) + 20; // Range notes[20, 120], distance[5, 60]
				note = Math.min(note, 120);
				note = Math.max(note, 20);
				System.out.println(String.format("Playing note %d", note));
				mc[5].noteOn(note, 600);
				Thread.sleep(50);
				mc[5].noteOff(note);
			}
		}
		System.out.println("Java is done, bye now.");
	}

//  static {
//    System.loadLibrary("OlivHCSR04");
//  }
}
