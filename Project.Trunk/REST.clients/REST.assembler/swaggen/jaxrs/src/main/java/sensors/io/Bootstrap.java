package sensors.io;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
    info = @Info(
        title = "Swagger Server",
        version = "1.0.0",
        description = "Simple example. Automatically turns a light on or off based on the ambient light. From this [Repo](https://github.com/OlivierLD/raspberry-coffee/tree/master/polo-shirt). ",
        termsOfService = "urn://Use-and-Reuse-at-will",
        contact = @Contact(email = "olivier@lediouris.net"),
        license = @License(
            name = "Apache 2.0",
            url = "http://www.apache.org/licenses/LICENSE-2.0.html"
        )
    )
)
public class Bootstrap extends HttpServlet {
}
