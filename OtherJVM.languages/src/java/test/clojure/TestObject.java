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

	public TestObject(String lastName, String firstname, Long age) {
		this.lastName = lastName;
		this.firstName = firstname;
		this.age = age;
	}

	public String getName() {
		return this.lastName;
	}

	public void setName(String name) {
		this.lastName = name;
	}

	public String getFirstname() {
		return this.firstName;
	}

	public void setFirstname(String vorname) {
		this.firstName = vorname;
	}

	public Long getAge() {
		return this.age;
	}

	public void setAge(Long age) {
		this.age = age;
	}

}
