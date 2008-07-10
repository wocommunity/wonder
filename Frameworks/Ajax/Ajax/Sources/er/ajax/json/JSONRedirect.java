package er.ajax.json;

/**
 * JSONRedirect represents a redirection to another JSON component.
 * 
 * @author mschrag
 */
public class JSONRedirect {
	private String _name;
	private String _url;

	/**
	 * Constructs a new JSONRedirect.
	 * 
	 * @param name the name of the next component
	 * @param url the URL to the next component
	 */
	public JSONRedirect(String name, String url) {
		_name = name;
		_url = url;
	}

	/**
	 * Returns the name of the next component.
	 * 
	 * @return the name of the next component
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the URL of the next component.
	 * 
	 * @return the URL of the next component
	 */
	public String getUrl() {
		return _url;
	}
}
