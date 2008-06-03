package er.ajax.example;

public class SimplePerson {
	private String _name;
	private int _age;

	public SimplePerson(String name, int age) {
		_name = name;
		_age = age;
	}

	public SimplePerson() {

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
	
	public int hashCode() {
		return _name.hashCode();
	}
	
	public boolean equals(Object obj) {
		return obj instanceof SimplePerson && ((SimplePerson)obj)._name.equals(_name);
	}
}