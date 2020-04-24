package etag;

import hash.MurmurHash;
import net.agkn.hll.HLL;

import javax.ws.rs.core.EntityTag;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * See https://github.com/aggregateknowledge/java-hll
 */
public class CollisionTester {

	public static EntityTag createETag(String stringifiedObject) {
		ETagGenerator etg = new ETagGenerator();
		return etg.createEtag(stringifiedObject);
	}

	private final static String CHARACTERS = "{}[]\\|!@#$%^&*()_+=-0987654321QWERTYUIOPpoiuytrewqASDFGHJKL:\"';lkjhgfdsaZXCVBNM<>?/.,mnbvcxz789-+6541230";
	private final static int MIN_STR_LENGTH =  10;
	private final static int MAX_STR_LENGTH = 100;

	private final static long MILLION = 1_000_000L;

	private final static long NB_ETAG_GEN = 1 * MILLION;

	private final static NumberFormat NF = NumberFormat.getInstance();
  private final static DecimalFormat DF = new DecimalFormat("##0.0000");

	private final static int REG_WIDTH =  8;
	private final static int LOG_2_M   = 30;

	public static void main(String... args) {
		final HLL hll = new HLL(LOG_2_M, REG_WIDTH); // (13/*log2m*/, 5/*registerWidth*/);

		for (long i=0; i<NB_ETAG_GEN; i++) {
			if (i % 10_000 == 0) {
				System.out.println(String.format("... Iteration #%s", NF.format(i)));
			}
			int strLen = MIN_STR_LENGTH + (int)Math.round(Math.ceil(Math.random() * (MAX_STR_LENGTH - MIN_STR_LENGTH)));
			StringBuffer sb = new StringBuffer();
			for (int idx=0; idx<strLen; idx++) {
				int cIdx = (int)Math.round(Math.floor(Math.random() * CHARACTERS.length()));
				sb.append(CHARACTERS.charAt(cIdx));
			}
			String string = sb.toString();
			long hash = MurmurHash.hash64(createETag(string).getValue());
      hll.addRaw(hash);
		}
		System.out.println(String.format("Put %s values, got back %s. Err %s %% (%f)", NF.format(NB_ETAG_GEN), NF.format(hll.cardinality()), DF.format(error(NB_ETAG_GEN, hll.cardinality())), 100 * error(NB_ETAG_GEN, hll.cardinality())));
	}

	private static double error(long reference, long actual) {
		long diff = Math.abs(actual - reference);
		return (double) diff / (double) reference;
	}
}
