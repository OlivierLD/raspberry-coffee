package test.clojure;

/**
 * To be used from Clojure.
 * See invokejava.clj
 */
public class TestObject {

	private String lastName;
	private String firstName;
	private Long age;

	public TestObject() {
		this.lastName = null;
		this.firstName = null;
		this.age = null;
	}

	public TestObject(String lastName, String firstName, Long age) {
		this.lastName = lastName;
		this.firstName = firstName;
		this.age = age;
	}

	public String getName() {
		return this.lastName;
	}

	public void setName(String name) {
		this.lastName = name;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstname) {
		this.firstName = firstname;
	}

	public Long getAge() {
		return this.age;
	}

	public void setAge(Long age) {
		this.age = age;
	}

}
