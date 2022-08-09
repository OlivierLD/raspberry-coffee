package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide smoothing service for generic type of data.
 *
 * @param <T> the type to smooth. See {@link Smoothable} interface
 */
public class DampingService<T> {

	private int maxLength;
	private final List<T> buffer;

	public DampingService (int maxLength) {
		if (maxLength < 1) {
			throw new RuntimeException("maxLength must be at least 1");
		}
		this.maxLength = maxLength; // Must be greater than 0
		buffer = new ArrayList<>();
	}

	public void resetBufferSize(int size) {
		this.maxLength = size;
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
