package restserver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // Class level
public @interface RootPath {
	String value() default "/"; // Must be named value for the prm name to be possibly omitted when using the annotation.
}
