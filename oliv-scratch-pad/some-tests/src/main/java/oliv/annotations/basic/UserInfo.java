package oliv.annotations.basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // Class level
public @interface UserInfo {
	public enum Priority {
		LOW, MEDIUM, HIGH
	}

	String userName() default "Oliv";
	Priority priority() default Priority.MEDIUM;
	String referenceDate() default "15-Aug-2019";
}
