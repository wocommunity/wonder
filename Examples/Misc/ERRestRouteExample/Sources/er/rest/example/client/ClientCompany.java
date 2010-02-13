package er.rest.example.client;


public class ClientCompany {
	private String _id;
	private String _name;

	public void setName(String name) {
		_name = name;
	}
	
	public void setId(String id) {
		_id = id;
	}
	
	public String getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}
	
	@Override
	public String toString() {
		return "[Company: id=" + _id + "; name=" + _name + "]";
	}
}
