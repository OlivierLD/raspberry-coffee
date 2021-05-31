package dnd.gui.splash;

public class Splasher {
	public static void main(String... args) {
		SplashWindow.splash(Splasher.class.getResource("paperboat.png"), null);
		SplashWindow.invokeMain("imagedb.gui.MainGUI", args);
		SplashWindow.disposeSplash();
	}
}
