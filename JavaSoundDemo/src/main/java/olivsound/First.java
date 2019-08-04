package olivsound;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

/**
 * First building block for a Theremin...
 */
public class First {
	public static void main(String... args)
	throws Exception {
		Synthesizer synth = MidiSystem.getSynthesizer();
		synth.open();

		final MidiChannel[] mc = synth.getChannels();
		Instrument[] instr = synth.getDefaultSoundbank().getInstruments();

		synth.loadInstrument(instr[90]);

		for (int i=0; i<120; i++) {
			mc[5].noteOn(i, 600);
			Thread.sleep(10);
			mc[5].noteOff(i);
		}
		for (int i=119; i>=0; i--) {
			mc[5].noteOn(i, 600);
			Thread.sleep(20);
			mc[5].noteOff(i);
		}
		Thread.sleep(1_000);
	}
}
