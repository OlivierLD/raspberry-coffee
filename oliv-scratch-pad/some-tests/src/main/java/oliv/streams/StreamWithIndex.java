package oliv.streams;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StreamWithIndex {
	private final static String LOREM_IPSUM = "Lorem ipsum dolor sit amet, at eam saepe efficiantur, pri reque suscipit te. No dicat persequeris vel. Ut periculis ocurreret delicatissimi est, duo ubique meliore contentiones no, ei veri fuisset corpora nec. Ius te putent sensibus, etiam propriae et vix. Blandit qualisque pri et. Eum in porro tempor efficiendi, eos dolores definiebas id. Nam ex legimus molestiae persequeris." +
			"Pro id dico audiam detracto, omnis iriure disputando vix ei. Eu vel pertinacia concludaturque, saperet efficiendi cotidieque pri ei, an nemore gloriatur sed. Erant essent ut vix, ius idque doctus ea. Per sint oportere tincidunt ut, quo ne prompta mediocritatem." +
			"Eam eu tamquam prodesset, ad mel corpora iracundia. Eu mea dictas democritum, id usu nonumy dissentias consectetuer. Ea mediocrem definitiones mei. Velit melius cu has." +
			"Cu per graece percipit postulant, ad iusto utinam recteque per. Has facer quaeque te. Pro et tation commodo comprehensam. Mei ea purto iusto, his assum laudem animal ne." +
			"No vel quis assum, sea no erant tractatos, mel ei nihil deserunt. Illum offendit dissentiet eu qui, habeo tollit id sea. Ei nam invidunt gloriatur. Id dicant ignota vim. Te has iudicabit deseruisse.";

	public static void main(String... args) {

		List<String> source = Arrays.asList(LOREM_IPSUM.split(" "));

		final AtomicInteger idx = new AtomicInteger(0);
		String mess = source.stream()
				.map(w -> w.replace(".", "").replace(",", ""))
				.filter(x -> { // The range evaluation happens in the filter
					boolean ok = idx.get() > 10 && idx.get() < 20;
					idx.set(idx.get() + 1);
					return ok;
				}).collect(Collectors.joining("\n"));
		System.out.println(mess);

		System.out.println("====================");
		mess = source.stream()
				.map(w -> w.replace(".", "").replace(",", ""))
				.skip(10)
				.limit(5)
				.collect(Collectors.joining("\n"));
		System.out.println(mess);

		// Find index of a string in the list
		System.out.println(String.format("Index of 'pertinacia' is %d", source.indexOf("pertinacia")));
	}
}
