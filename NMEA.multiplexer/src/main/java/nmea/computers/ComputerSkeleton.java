package nmea.computers;

import nmea.api.Multiplexer;
import nmea.api.NMEAParser;
import nmea.parser.StringParsers;

import java.util.Arrays;
import java.util.List;
import utils.StringUtils;

/**
 * Computer Skeleton
 */
public class ComputerSkeleton extends Computer {

	private final static String DEFAULT_PREFIX = "XX";
	private final List<String> requiredStrings = Arrays.asList(new String[]{"RMC"});

	private String generatedStringsPrefix = DEFAULT_PREFIX;

	public ComputerSkeleton(Multiplexer mux) {
	  this(mux, DEFAULT_PREFIX);
	}

	public void setPrefix(String prefix) {
		this.generatedStringsPrefix = prefix;
	}

	public ComputerSkeleton(Multiplexer mux, String prefix) {
		super(mux);
		if (prefix == null || prefix.length() != 2) {
			throw new RuntimeException("Prefix must exist, and be EXACTLY 2 character long.");
		}
		this.generatedStringsPrefix = prefix;
	}

	/**
	 * Receives the data, and potentially produces new ones.
	 *
	 * @param mess Received message
	 */
	@Override
	public void write(byte[] mess) {
		String sentence = new String(mess);
		if (StringParsers.validCheckSum(sentence)) {
			String sentenceID = StringParsers.getSentenceID(sentence);
			if (!generatedStringsPrefix.equals(StringParsers.getDeviceID(sentence)) &&  // IMPORTANT: To prevent re-computing of computed data.
						requiredStrings.contains(sentenceID)) {
				// This computer changes the device ID of RMC sentence, that's all.
				String content = sentence.substring(1, sentence.indexOf("*"));
				String s = this.generatedStringsPrefix + content.substring(2);
				// Recompute the Checksum
				int cs = StringParsers.calculateCheckSum(s);
				String newStr = "$" + s + "*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0") + NMEAParser.STANDARD_NMEA_EOS;
				this.produce(newStr);
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop Computing True Wind, " + this.getClass().getName());
	}

	public static class ComputerSkeletonBean { // TODO: implement a trait
		private String cls;
		private String type = "skeleton";
		private boolean verbose;

		public ComputerSkeletonBean(ComputerSkeleton instance) {
			this.cls = instance.getClass().getName();
			this.verbose = instance.isVerbose();
		}
	}

	@Override
	public Object getBean() {
		return new ComputerSkeletonBean(this);
	}
}
