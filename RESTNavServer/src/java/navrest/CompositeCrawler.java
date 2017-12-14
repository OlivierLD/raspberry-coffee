package navrest;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Reads web/catalog/catalog.js to find the compoisite definitions
 * Crawls the directory under web/ matching [0..9]{4}/[0..9]{2}/[0..9]{2}/<composite-key>_[0..9]{6}
 */
public class CompositeCrawler {

	private final static String COMPOSITE_CATALOG = "web" + File.separator + "catalog" + File.separator + "catalog.js";

	private final static String NASHORN_ARGS = "nashorn.args";
	private final static String ES_6 = "--language=es6";

	private final Pattern YEAR_PATTERN = Pattern.compile("^\\d{4}$");
	private final Pattern MONTH_PATTERN = Pattern.compile("^\\d{2}$");
	private final Pattern DAY_PATTERN = MONTH_PATTERN;

	private final Pattern[] PATTERNS = new Pattern[] {
			YEAR_PATTERN,
			MONTH_PATTERN,
			DAY_PATTERN
	};

	private Map<String, Object> getCompositeCatalog() throws Exception {
		File catalog = new File(COMPOSITE_CATALOG);
		if (!catalog.exists()) {
			throw new RuntimeException(catalog.getAbsolutePath() + " Not found");
		}

		System.setProperty(NASHORN_ARGS, ES_6);

		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("nashorn");
		engine.eval(new FileReader(catalog));
		Object catalogObject = engine.eval("compositeCatalog"); // as named in the script.

		return (Map<String, Object>)catalogObject;
	}

	private List<Pattern> buildPatternList() throws Exception {
		Map<String, Object> catalog = getCompositeCatalog();
		List<Pattern> patternlist = new ArrayList<>();
		catalog.keySet().forEach(k -> {
			Map<String, Object> composite = (Map<String, Object>)catalog.get(k);
			String key = (String)composite.get("key");
			Pattern pattern = Pattern.compile(String.format("^%s_\\d{6}$", key));
			patternlist.add(pattern);
		});
		return patternlist;
	}

	private Map<String, Object> crawl(File from, int level, List<Pattern> pList, Map<String, Object> compositeTree) {
		if (from.isDirectory()) {
			Arrays.stream(from.listFiles()).forEach(f -> {
				if (f.isDirectory()) {
					String name = f.getName();
					// Match ?
					if (PATTERNS[level].matcher(name).matches()) {
//					System.out.println(String.format(" -> %s", name));
						if (level < (PATTERNS.length - 1)) {
							Map<String, Object> subMap = new HashMap<>();
							compositeTree.put(name, subMap);
							crawl(f, level + 1, pList, subMap);
						} else {
							// If directory starts with a composite key name, followed by '_' and 6 digits, then ok.
							List<String> compositeList = new ArrayList<>();
							Arrays.stream(f.listFiles()).forEach(d -> {
								if (matchesOneOf(d.getName(), pList)) {
//								System.out.println(String.format("%s", d.getName()));
									compositeList.add(d.getName());
								}
							});
							compositeTree.put(name, compositeList);
						}
					}
				}
			});
		}
		return compositeTree;
	}

	private boolean matchesOneOf(String str, List<Pattern> pList) {
		for (Pattern pattern : pList) {
			if (pattern.matcher(str).matches()) {
				return true;
			}
		};
		return false;
	}

	public Map<String, Object> getCompositeHierarchy() throws Exception {
		Map<String, Object> composites = new HashMap<>();
		composites = this.crawl(new File("web"), 0, this.buildPatternList(), composites);
		return composites;
	}

	public static void main(String... args) throws Exception {
		CompositeCrawler crawler = new CompositeCrawler();
		Map<String, Object> composites = crawler.getCompositeHierarchy();
		System.out.println("-");
	}
}
