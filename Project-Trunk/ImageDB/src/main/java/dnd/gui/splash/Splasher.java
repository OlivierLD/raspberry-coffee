package dnd.gui.splash;

public class Splasher {

	public static void main(String... args) {
		SplashWindow.splash(Splasher.class.getResource("paperboat.png"), null);
		SplashWindow.invokeMain("dnd.gui.MainGUI", args);
//		try {
//			Thread.sleep(2_000L);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
		SplashWindow.disposeSplash();
	}

}
