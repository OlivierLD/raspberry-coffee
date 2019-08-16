package oliv.annotations;

import java.util.Date;

@UserInfo (
		priority = UserInfo.Priority.HIGH,
		userName = "Olivier",
		referenceDate = "15-Aug-2019 00:00:00"
)
public class AnnotatedExample {

	@VerboseInfo()
	public void methodOne() {

	}

	@VerboseInfo(verbose = true)
	public void methodTwo() {

	}

	public void methodThree() {

	}
}
