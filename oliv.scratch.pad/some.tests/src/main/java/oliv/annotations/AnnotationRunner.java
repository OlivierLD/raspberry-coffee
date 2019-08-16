package oliv.annotations;

//import java.lang.annotation.Annotation;

import java.lang.reflect.Method;

public class AnnotationRunner {

	public static void main(String... args) {

		Class<AnnotatedExample> obj = AnnotatedExample.class;
		if (obj.isAnnotationPresent(UserInfo.class)) {
//			Annotation annotation = obj.getAnnotation(UserInfo.class);
			UserInfo userInfo = obj.getAnnotation(UserInfo.class); // (UserInfo)annotation;
			System.out.println(String.format("Priority : %s", userInfo.priority()));
			System.out.println(String.format("User Name: %s", userInfo.userName()));
			System.out.println(String.format("Date     : %s", userInfo.referenceDate()));
		} else {
			System.out.println("No UserInfo annotation found");
		}
		for (Method method : obj.getDeclaredMethods()) {
			if (method.isAnnotationPresent(VerboseInfo.class)) {
				VerboseInfo verboseInfo = method.getAnnotation(VerboseInfo.class);
				System.out.println(String.format("Method %s, verbose: %s.", method.getName(), verboseInfo.verbose()));
			} else {
				System.out.println(String.format("Method %s is not annotated.", method.getName()));
			}
		}
	}
}
