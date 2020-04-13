package oliv.annotations.interceptors;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@SecurityCheck
public class SecurityCheckInterceptor {

	@AroundInvoke
	public Object checkSecurity(InvocationContext context) throws Exception {

		System.out.println(String.format("In %s", this.getClass().getName()));
		/* check the parameters or do a generic security check before invoking the original method */
		Object[] params = context.getParameters();

		/* if security validation fails, you can throw an exception */

		/* invoke the proceed() method to call the original method */
		Object ret = context.proceed();

		/* perform any post method call work */
		return ret;
	}
}
