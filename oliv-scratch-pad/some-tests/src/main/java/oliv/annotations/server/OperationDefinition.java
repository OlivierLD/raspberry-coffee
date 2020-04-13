package oliv.annotations.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) // Method level
public @interface OperationDefinition {
	public enum Verb {
		GET, POST, PUT, DELETE
	}
	Verb verb() default Verb.GET;
	String path() default "";
	String description() default "";
}
