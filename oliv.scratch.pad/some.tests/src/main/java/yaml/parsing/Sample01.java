package yaml.parsing;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
//import java.util.Arrays;
//import java.util.Iterator;
import java.util.Map;

public class Sample01 {

	private final static String YAML_FILE = "flow.yaml"; // "customer.yaml"

	public void go() throws Exception {
		Yaml yaml = new Yaml();
		InputStream inputStream = new FileInputStream(YAML_FILE);
		Map<String, Object> map = yaml.load(inputStream);
		System.out.println("Map:" + map);
//		Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
//		while (iterator.hasNext()) {
//			Map.Entry<String, Object> next = iterator.next();
//			System.out.println(String.format("%s -> %s", next.getKey(), next.getValue().getClass().getName()));
//		}
		map.keySet().forEach(k -> {
			System.out.println(String.format("%s -> %s", k, map.get(k).getClass().getName()));
		});
	}

	public static void main(String... args) {
		Sample01 sample01 = new Sample01();
		try {
			sample01.go();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
