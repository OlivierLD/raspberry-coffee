package utils;

import java.util.ArrayList;
import java.util.List;

public class DampingService<T> {

	private int maxLength;
	private List<T> buffer;

	public DampingService (int maxLength) {
		this.maxLength = maxLength; // Must be greater than 0
		buffer = new ArrayList<>();
	}

	public void append(Smoothable<T> element) {
		this.buffer.add(element.get());
		while (buffer.size() > maxLength) {
			buffer.remove(0);
		}
	}

	public T smooth(Smoothable<T> smoothed) {
		return smoothed.smooth(buffer);
	}

	public int getBufferSize() {
		return buffer.size();
	}

	public interface Smoothable<T> {
		T get();
		void accumulate(T elmt);
		T smooth(List<T> buffer);
	}

}
