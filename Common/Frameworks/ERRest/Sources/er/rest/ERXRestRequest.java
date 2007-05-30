package er.rest;

public class ERXRestRequest {
	private ERXRestKey _key;
	private ERXRestRequestNode _rootNode;

	public ERXRestRequest(ERXRestKey key, ERXRestRequestNode rootNode) {
		_key = key;
		_rootNode = rootNode;
	}
	
	public ERXRestKey key() {
		return _key;
	}

	public ERXRestRequestNode rootNode() {
		return _rootNode;
	}
}
