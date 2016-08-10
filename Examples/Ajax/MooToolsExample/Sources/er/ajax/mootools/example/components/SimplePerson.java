package er.ajax.mootools.example.components;

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
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof SimplePerson && ((SimplePerson)obj)._name.equals(_name);
	}
}