package restserver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface QueryParam {
	enum PrmTypes {
		STRING, INT, SHORT, FLOAT, DOUBLE, BOOLEAN
	}
	String name() default "prm";
	PrmTypes type() default PrmTypes.STRING;
}
