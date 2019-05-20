package rpi;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	/**
	 *
	 * @param args managed arg --help, -h
	 */
	public static void main(String... args) {
		SpringApplication.run(Application.class, args);
		System.out.println(String.format(">> main method: Arguments, %d element(s)", args.length));
		if (Arrays.asList(args).contains("--help") ||
				Arrays.asList(args).contains("-h")) {
			System.out.println("-----------------");
			System.out.println("Managed System properties:");
			System.out.println("-----------------");
			System.out.println(String.format("%s, \tdefault %s", "server.verbose", "false"));
			System.out.println("- For the relay");
			System.out.println(String.format("%s, \tdefault %s", "relay.verbose", "false"));
			System.out.println(String.format("%s, \tdefault %s", "relay.map", "1:11"));
			System.out.println("- For the MCP3008");
			System.out.println(String.format("%s, \tdefault %s", "miso.pin", "0"));
			System.out.println(String.format("%s, \tdefault %s", "mosi.pin", "10"));
			System.out.println(String.format("%s, \tdefault %s", "clk.pin", "11"));
			System.out.println(String.format("%s, \tdefault %s", "cs.pin", "8"));
			System.out.println(String.format("%s, \tdefault %s", "adc.channel", "0"));
			System.out.println("-----------------");
		}
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println(String.format("In commandLineRunner, Arguments, %d element(s)", args.length));
			Arrays.asList(args).forEach(arg -> System.out.println(String.format("Arg: %s", arg)));

			System.out.println("Now, let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}

		};
	}

}
