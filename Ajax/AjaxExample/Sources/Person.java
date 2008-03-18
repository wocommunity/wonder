

public class Person {
	private String _name;
	private int _age;

	public Person(String name, int age) {
		_name = name;
		_age = age;
	}

	public Person() {

	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public int getAge() {
		return _age;
	}

	public void setAge(int age) {
		_age = age;
	}
}