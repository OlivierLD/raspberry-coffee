package computers;

import nmea.api.Multiplexer;
import nmea.parser.StringGenerator;
import nmea.parser.StringParsers;

public class TrueWindComputer extends Computer {

	public TrueWindComputer(Multiplexer mux){
		super(mux);
	}

	/**
	 * Receives the data, and potentially produces new ones.
	 * @param mess
	 */
	@Override
	public void write(byte[] mess) {
		String sentence = new String(mess);
		if (StringParsers.validCheckSum(sentence)) {
			String sentenceID = StringParsers.getSentenceID(sentence);
			if ("RND".equals(sentenceID)) { // Just an example
				String newSentence = StringGenerator.gerenateVWT("XX", 12.34, 273);
				this.produce(newSentence); // In the superCalss
			}
		}
	}

	@Override
	public void close() {

	}

	@Override
	public Object getBean() {
		return null;
	}
}
