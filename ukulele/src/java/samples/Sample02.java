package samples;

//import org.jfugue.pattern.Pattern;
import org.jfugue.Pattern;
// import org.jfugue.player.Player;
import org.jfugue.Player;

public class Sample02 {
	public static void main(String... args) {
		Pattern p1 = new Pattern("V0 I[Piano] Eq Ch. | Eq Ch. | Dq Eq Dq Cq");
		Pattern p2 = new Pattern("V1 I[Flute] Rw     | Rw     | GmajQQQ  CmajQ");
		Player player = new Player();
//	player.play(p1, p2);
	}
}
