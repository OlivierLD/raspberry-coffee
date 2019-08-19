package restserver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface QueryParam {
	String STRING = "java.lang.String";
	String SHORT = "short";
	String SHORT_OBJECT = "java.lang.Short";
	String INT = "int";
	String INTEGER = "java.lang.Integer";
	String FLOAT = "float";
	String FLOAT_OBJECT = "java.lang.Float";
	String DOUBLE = "double";
	String DOUBLE_OBJECT = "java.lang.Double";
	String BOOLEAN = "boolean";

	List<String> supportedTypes = Arrays.asList(new String[] {
		STRING, SHORT, SHORT_OBJECT, INT, INTEGER, FLOAT, FLOAT_OBJECT, DOUBLE, DOUBLE_OBJECT, BOOLEAN
	});
	String name() default "prm";
}
