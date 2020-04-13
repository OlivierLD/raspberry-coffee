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

		sb = new StringBuffer();
		sb.append((char)65);
		sb.append((char)107);
		sb.append((char)101);
		sb.append((char)117);
		sb.append((char)32);
		sb.append((char)99);
		sb.append((char)111);
		sb.append((char)117);
		sb.append((char)99);
		sb.append((char)111);
		sb.append((char)117);
		System.out.println(String.format("[%s], len:%d ", sb.toString(), sb.length()));
	}
}
