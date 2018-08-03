package oliv.misc;

public class StringBufferTest {

	public static void main(String... args) {
		StringBuffer sb = new StringBuffer();
		sb.append("Pouet-pouet");
		System.out.println(String.format("[%s], len:%d ", sb.toString(), sb.length()));
		sb.delete(0, sb.length());
		System.out.println(String.format("[%s], len:%d ", sb.toString(), sb.length()));
		sb.append("Tagada");
		System.out.println(String.format("[%s], len:%d ", sb.toString(), sb.length()));
	}
}
