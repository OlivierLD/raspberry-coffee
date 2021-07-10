package samples;

import i2c.sensor.VCNL4000;

import com.pi4j.system.SystemInfo;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class VCNL4000ProximityWithSound {
	private static boolean go = true;
	private final static int MIN_AMBIENT = 0;
	private final static int MAX_AMBIENT = 5_500;

	public final static float SAMPLE_RATE = 8_000f;

	public static void tone(int hz, int mSecs) throws LineUnavailableException {
		tone(hz, mSecs, 1.0);
	}

	public static void tone(int hz, int mSecs, double vol) throws LineUnavailableException {
		byte[] buf = new byte[1];
		AudioFormat af = new AudioFormat(SAMPLE_RATE, // sampleRate
						8,           // sampleSizeInBits
						1,           // channels
						true,        // signed
						false);      // bigEndian
		SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
		sdl.open(af);
		sdl.start();
		for (int i = 0; i < mSecs * 8; i++) {
			double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
			buf[0] = (byte) (Math.sin(angle) * 127.0 * vol);
			sdl.write(buf, 0, 1);
		}
		sdl.drain();
		sdl.stop();
		sdl.close();
	}

	public static void main(String... args) throws Exception {
		VCNL4000 sensor = new VCNL4000();
		int prox = 0;
		int ambient = 0;

		// Bonus : CPU Temperature
		try {
			System.out.println("CPU Temperature   :  " + SystemInfo.getCpuTemperature());
			System.out.println("CPU Core Voltage  :  " + SystemInfo.getCpuVoltage());
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		final BeepThread beeper = new BeepThread();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			beeper.stopBeeping();
			System.out.println("\nBye");
		}, "Shutdown Hook"));
		System.out.println("-- Ready --");
		beeper.start();
		while (go) //  && i++ < 5)
		{
			try {
				//      prox = sensor.readProximity();
				int[] data = sensor.readAmbientProximity();
				prox = data[VCNL4000.PROXIMITY_INDEX];
				ambient = data[VCNL4000.AMBIENT_INDEX];
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			}
			System.out.println("Ambient:" + ambient + ", Proximity: " + prox); //  + " unit?");
			int amb = 100 - Math.min((int) Math.round(100f * ((float) ambient / (float) (MAX_AMBIENT - MIN_AMBIENT))), 100);
			beeper.setAmbient(amb);
			try {
				Thread.sleep(100L);
			} catch (InterruptedException ex) {
				System.err.println(ex.toString());
			}
		}
	}

	private static class BeepThread extends Thread {
		private int amb = 0; // 0 - 100   0: far, 100:Cannot be closer
		private boolean go = true;

		public void setAmbient(int amb) {
			this.amb = amb;
		}

		public void run() {
			while (go) {
				try {
					tone(1000 + (10 * amb), 100);
					Thread.sleep(550 - (5 * amb));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		public void stopBeeping() {
			this.go = false;
		}
	}
}
