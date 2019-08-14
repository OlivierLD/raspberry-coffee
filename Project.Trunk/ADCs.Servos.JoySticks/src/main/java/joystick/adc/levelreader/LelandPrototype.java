package joystick.adc.levelreader;

import adc.utils.EscapeSeq;
import org.fusesource.jansi.AnsiConsole;
import joystick.adc.levelreader.manager.AirWaterOilInterface;
import joystick.adc.levelreader.manager.SevenADCChannelsManager;
import utils.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

public class LelandPrototype implements AirWaterOilInterface {
	private static LevelMaterial<Float, SevenADCChannelsManager.Material>[] data = null;
	private final static NumberFormat DF31 = new DecimalFormat("000.0");
	private final static NumberFormat DF4 = new DecimalFormat("###0");

	public LelandPrototype() {
		data = new LevelMaterial[7];
		for (int i = 0; i < data.length; i++) {
			data[i] = new LevelMaterial<Float, SevenADCChannelsManager.Material>(0f, SevenADCChannelsManager.Material.UNKNOWN);
		}
	}

	private static String materialToString(SevenADCChannelsManager.Material material) {
		String s = "UNKNOWN";
		if (material == SevenADCChannelsManager.Material.AIR)
			s = "Air";
		else if (material == SevenADCChannelsManager.Material.WATER)
			s = "Water";
		else if (material == SevenADCChannelsManager.Material.OIL)
			s = "Oil";
		return s;
	}

	private static void displayData() {
		// Clear the screen, cursor on top left.
		// AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
		AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1));
		String str = "+---+--------+---------+";
		AnsiConsole.out.println(str);
		str = "| C |  Vol % |   Mat   |";
		AnsiConsole.out.println(str);

		str = "+---+--------+---------+";
		AnsiConsole.out.println(str);
		for (int chan = data.length - 1; chan >= 0; chan--) {
			str = "| " + Integer.toString(chan) + " | " +
					StringUtils.lpad(DF4.format(data[chan].getPercent()), 4, " ") + " % | " +
					StringUtils.lpad(materialToString(data[chan].getMaterial()), 7, " ") + " |";

			AnsiConsole.out.println(str);
		}
		str = "+---StringUtils.+--------+---------+";
		AnsiConsole.out.println(str);
	}

	@Override
	public void setTypeOfChannel(int channel, SevenADCChannelsManager.Material material, float val) {
		data[channel] = new LevelMaterial(val, material);
		displayData();
		// Debug
		AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 14 + channel));
		Date now = new Date();
		AnsiConsole.out.println(now.toString() + ": Channel " + channel + " >> (" + DF31.format(val) + ") " + materialToString(material) + "       ");
	}

	public static void main(String... args) throws Exception {
		System.out.println(args.length + " parameter(s).");
		LelandPrototype lp = new LelandPrototype();
		final SevenADCChannelsManager sac = new SevenADCChannelsManager(lp);
		// CLS
		AnsiConsole.out.println(EscapeSeq.ANSI_CLS);

		final Thread me = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println();
				sac.quit();
				synchronized (me) {
					me.notify();
				}
				System.out.println("Program stopped by user's request.");
			}, "Shutdown Hook"));
		synchronized (me) {
			System.out.println("Main thread waiting...");
			me.wait();
		}
		System.out.println("Done.");
	}

	private abstract static class Tuple<X, Y> {
		public final X x;
		public final Y y;

		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "(" + x + "," + y + ")";
		}

		@Override
		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}
			if (other == this) {
				return true;
			}
			if (!(other instanceof Tuple)) {
				return false;
			}
			Tuple<X, Y> other_ = (Tuple<X, Y>) other;
			return other_.x == this.x && other_.y == this.y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((x == null) ? 0 : x.hashCode());
			result = prime * result + ((y == null) ? 0 : y.hashCode());
			return result;
		}
	}

	public static class LevelMaterial<X, Y> extends Tuple<X, Y> {
		public LevelMaterial(X x, Y y) {
			super(x, y);
		}

		public X getPercent() {
			return this.x;
		}

		public Y getMaterial() {
			return this.y;
		}
	}
}
