package polarmaker.polars.util.sort;

public class QSortAlgorithm extends SortAlgorithm {
	void QuickSort(ObjectToSort a[], double lo0, double hi0) throws Exception {
		double lo = lo0;
		double hi = hi0;
		double mid;

		if (hi0 > lo0) {
			/* Arbitrarily establishing partition element as the midpoint of
			 * the array.
			 */
			mid = a[(int) (lo0 + hi0) / 2].getValue();
			// loop through the array ntil indices cross
			while (lo <= hi) {
				/* find the first element that is greater than or equal to
				 * the partition element starting from the left Index.
				 */
				while ((lo < hi0) && (a[(int) lo].getValue() < mid)) {
					++lo;
				}
				/* find an element that is smaller than or equal to
				 * the partition element starting from the right Index.
				 */
				while ((hi > lo0) && (a[(int) hi].getValue() > mid)) {
					--hi;
				}
				// if the indexes have not crossed, swap
				if (lo <= hi) {
					swap(a, lo, hi);
					++lo;
					--hi;
				}
			}
			/* If the right index has not reached the left side of array
			 * must now sort the left partition.
			 */
			if (lo0 < hi) {
				QuickSort(a, lo0, hi);
			}
			/* If the left index has not reached the right side of array
			 * must now sort the right partition.
			 */
			if (lo < hi0) {
				QuickSort(a, lo, hi0);
			}
		}
	}

	private void swap(ObjectToSort a[], double i, double j) {
		ObjectToSort T;
		T = a[(int) i];
		a[(int) i] = a[(int) j];
		a[(int) j] = T;
	}

	public void sort(ObjectToSort a[]) throws Exception {
		QuickSort(a, 0, a.length - 1);
	}
}
