package rest.oas;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Swagger;
import rest.oas.impl.ADCChannel;
import rest.oas.impl.RelayManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class Bootstrap extends HttpServlet {
	@Override
	public void init(ServletConfig config) throws ServletException {
		Info info = new Info()
				.title("OpenAPI Server")
				.description("Simple example. Automatically turns a light on or off based on the ambient light. From this [Repo](https://github.com/OlivierLD/raspberry-coffee/tree/master/polo-shirt). ")
				.termsOfService("urn://Use-and-Reuse-at-will")
				.contact(new Contact()
						.email("olivier@lediouris.net"))
				.license(new License()
						.name("Apache 2.0")
						.url("http://www.apache.org/licenses/LICENSE-2.0.html"));

		ServletContext context = config.getServletContext();
		Swagger swagger = new Swagger().info(info);

		ADCChannel adcChannel = null;
		RelayManager relayManager = null;
		try {
			adcChannel = new ADCChannel();
		} catch (Throwable ex) {
			System.err.println("Exception creating ADCChannel");
			ex.printStackTrace();
		}

		try {
			relayManager = new RelayManager("1:11");
		} catch (Throwable ex) {
			System.err.println("Exception creating RelayManager");
			ex.printStackTrace();
		}

		context.setAttribute("adc-channel", adcChannel);
		context.setAttribute("relay-manager", relayManager);

		new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
	}
}
