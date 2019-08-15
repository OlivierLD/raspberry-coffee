package oliv.annotations;

@UserInfo (
		priority = UserInfo.Priority.HIGH,
		userName = "Olivier"
)
public class AnnotatedExample {

	@VerboseInfo(verbose = false)
	public void methodOne() {

	}

	@VerboseInfo(verbose = true)
	public void methodTwo() {

	}
}
