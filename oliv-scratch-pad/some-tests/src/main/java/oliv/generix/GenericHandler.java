package oliv.generix;

import java.util.Arrays;

public class GenericHandler<X> {

	private X storage;

	public void put(X object) {
		this.storage = object;
	}

	public X get() {
		return this.storage;
	}

	public static <T> GenericHandler<T> of(T... obj) {
		GenericHandler gh = new GenericHandler<T>();
		Arrays.asList(obj).forEach(gh::put);
//		for (T o : obj) {
//			gh.put(o);
//		}
		return gh;
	}
}

