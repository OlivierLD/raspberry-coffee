package navrest;

import http.HTTPServer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reads web/catalog/catalog.js to find the composite definitions
 * Crawls the directory under web/ matching [0..9]{4}/[0..9]{2}/[0..9]{2}/<composite-key>_[0..9]{6}
 */
public class CompositeCrawler {

	private final static String COMPOSITE_CATALOG = "web" + File.separator + "catalog" + File.separator + "catalog.js";

	private final static String NASHORN_ARGS = "nashorn.args";
	private final static String ES_6 = "--language=es6";

	private final Pattern YEAR_PATTERN  = Pattern.compile("^\\d{4}$");
	private final Pattern MONTH_PATTERN = Pattern.compile("^\\d{2}$");
	private final Pattern DAY_PATTERN   = MONTH_PATTERN;

	private final Pattern[] PATTERNS = new Pattern[] {
			YEAR_PATTERN,
			MONTH_PATTERN,
			DAY_PATTERN
	};

	private FilenameFilter compositeElementFilter = (dir, name) -> {
		if ((name.startsWith("_") && name.endsWith(".png")) || name.equals("grib.grb")) {
			return true;
		} else {
			return false;
		}
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
		List<Pattern> patternList = new ArrayList<>();
		catalog.keySet().forEach(k -> {
			Map<String, Object> composite = (Map<String, Object>)catalog.get(k);
			String key = (String)composite.get("key");
			Pattern pattern = Pattern.compile(String.format("^%s_\\d{6}$", key));
			patternList.add(pattern);
		});
		return patternList;
	}



	private Map<String, Object> crawl(File from, int level, List<Pattern> pList, Map<String, Object> compositeTree, String filter) {

		Comparator<File> fileComparator = Comparator.comparing(File::getName);

		if (from.isDirectory()) {
			List<File> fromList = Arrays.asList(from.listFiles());
			Collections.sort(fromList, fileComparator);

//			System.out.println("-- Sorted --");
//			fromList.stream().forEach(System.out::println);
//			System.out.println("------------");

			fromList.stream().forEach(f -> {
				if (f.isDirectory()) {
					String name = f.getName();
					// Match ?
					if (PATTERNS[level].matcher(name).matches()) {
// 					System.out.println(String.format(" -> %s", name));
						if (level < (PATTERNS.length - 1)) {
							Map<String, Object> subMap = new TreeMap<>();
							compositeTree.put(name, subMap);
							crawl(f, level + 1, pList, subMap, filter);
						} else {
							// If directory starts with a composite key name, followed by '_' and 6 digits, then ok.
							List<CompositeDescription> compositeList = new ArrayList<>();
							List<File> fList = Arrays.asList(f.listFiles());
							Collections.sort(fList);
							fList.stream().forEach(d -> {
								if (d.isDirectory() && matchesOneOf(d.getName(), pList)) {

									if (filter == null || filter.isEmpty() || (filter != null && d.getName().contains(filter))) {
//									System.out.println(String.format(">> Sorted %s, level %d, filter %s", d.getName(), level, filter));
										CompositeDescription compositeDescription = new CompositeDescription().name(d.getName());
										List<CompositeElement> elements = new ArrayList<>();
										// Now scan the files in this directory
										File[] compositeElements = d.listFiles(compositeElementFilter);
										List<File> fileList = Arrays.asList(compositeElements);
										Collections.sort(fileList);
										fileList.stream().forEach(fName -> {
	//									System.out.println(String.format("  >> %s : file:%s", fName.getName(), fName.getAbsolutePath()));
											CompositeElement element = new CompositeElement()
													.type(fName.getName().equals("grib.grb") ? ElementType.GRIB : ElementType.FAX)
													.name(fName.getName())
													.resource("file:" + fName.getAbsolutePath());
											elements.add(element);
										});
										compositeDescription = compositeDescription.compositeElements(elements);
										compositeList.add(compositeDescription);
									}
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

	public Map<String, Object> getCompositeHierarchy(String filter) throws Exception {
		Map<String, Object> composites = new TreeMap<>();
		composites = this.crawl(new File("web"), 0, this.buildPatternList(), composites, filter);
		// Done. Now cut empty branches
//	System.out.println("Tree First Pass completed");
		Set<String> years = composites.keySet();
		List<String> yearsToDelete = new ArrayList<>();
		for (String year : years) {
			List<String> monthsToDelete = new ArrayList<>();
			TreeMap<String, Object> monthsMap = (TreeMap<String, Object>)composites.get(year);
//		System.out.println("monthsMap");
			for (String month : monthsMap.keySet()) {
				List<String> daysToDelete = new ArrayList<>();
				TreeMap<String, Object> daysMap = (TreeMap<String, Object>)monthsMap.get(month);
//			System.out.println("daysMap");
				Set<String> days = daysMap.keySet();
				for (String day : days) {
					List<Object> compositesList = (List<Object>)daysMap.get(day);
//				System.out.println(String.format("Day %s: %d elements", day, compositesList.size()));
					if (compositesList.size() == 0) {
						// Cut that branch!
//					System.out.println(String.format(">>> Cut branch for day %s", day));
						daysToDelete.add(day); // Mark it to be deleted
					}
				}
				daysToDelete.stream()
						.forEach(d -> {
							daysMap.remove(d);
						});
				if (daysMap.size() == 0) {
					monthsToDelete.add(month);
				}
//			System.out.println(String.format(">> Month %s", month));
			}
			monthsToDelete.stream()
					.forEach(m -> {
						monthsMap.remove(m);
					});
//		System.out.println(String.format(">> Year %s", year));
			if (monthsMap.size() == 0) {
				yearsToDelete.add(year);
			}
		}
		if (yearsToDelete.size() > 0) {
			Map<String, Object> finalMap = composites;
			yearsToDelete.stream()
					.forEach(y -> {
						finalMap.remove(y);
					});
			composites = finalMap;
		}
		return composites;
	}

	public static class CompositeDescription {
		String name;
		List<CompositeElement> compositeElements;

		public CompositeDescription name(String name) {
			this.name = name;
			return this;
		}
		public CompositeDescription compositeElements(List<CompositeElement> compositeElements) {
			this.compositeElements = compositeElements;
			return this;
		}
	}

	enum ElementType {
		FAX, GRIB
	};

	public static class CompositeElement {
		ElementType type;
		String name;
		String resource;

		public CompositeElement type(ElementType type) {
			this.type = type;
			return this;
		}
		public CompositeElement name(String name) {
			this.name = name;
			return this;
		}
		public CompositeElement resource(String resource) {
			this.resource = resource;
			return this;
		}
	}

	/* For tests */
	public static void main(String... args) throws Exception {
		CompositeCrawler crawler = new CompositeCrawler();
		Map<String, Object> composites = crawler.getCompositeHierarchy("PAC-0001");

		String[] blah = new String[] { "Ah!" };
		try {
			String x = blah[10]; // Bam!
			System.out.println("Ok");
		} catch (Exception ex) {
			System.out.println("Boom:");
			List<String> stack = HTTPServer.dumpException(ex);
			System.out.println(ex.toString());
			stack.stream()
					.forEach(System.out::println);
		}
		System.out.println("\nDone.");
	}
}
