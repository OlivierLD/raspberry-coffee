package yaml.parsing;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class SampleMUX {

	private final static String YAML_FILE = "multiplexer.yaml";

	private void dumpChannel(Map<String, Object> channel) {
		System.out.println(String.format("Channel %s", channel));
	}

	private void dumpForwarder(Map<String, Object> forwarder) {
		System.out.println(String.format("Forwarder %s", forwarder));
	}

	private void dumpComputer(Map<String, Object> computer) {
		System.out.println(String.format("Computer %s", computer));
	}

	public void go() throws Exception {
		Yaml yaml = new Yaml();
		InputStream inputStream = new FileInputStream(YAML_FILE);
		Map<String, Object> map = yaml.load(inputStream);
		map.keySet().forEach(k -> {
			System.out.println(String.format("%s -> %s", k, map.get(k).getClass().getName()));
			switch (k) {
				case "name":
					System.out.println(String.format("Name: %s", map.get(k)));
					break;
				case "context":
					Map<String, Object> context = (Map<String, Object>)map.get(k);
					System.out.println(context);
					break;
				case "channels":
					List<Map<String, Object>> channels = (List<Map<String, Object>>)map.get(k);
					channels.stream().forEach(this::dumpChannel);
					break;
				case "forwarders":
					List<Map<String, Object>> forwarders = (List<Map<String, Object>>)map.get(k);
					forwarders.stream().forEach(this::dumpForwarder);
					break;
				case "computers":
					List<Map<String, Object>> computers = (List<Map<String, Object>>)map.get(k);
					computers.stream().forEach(this::dumpComputer);
					break;
				default:
					break;
			}
		});
	}

	public static void main(String... args) {
		SampleMUX muxDef = new SampleMUX();
		try {
			muxDef.go();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
