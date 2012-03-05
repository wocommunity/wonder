package er.rest.entityDelegates;

import er.rest.ERXRestException;
import er.rest.ERXRestRequestNode;

/**
 * ERXRestRequest encapsulates the state of a rest request.
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public class ERXRestRequest {
	private ERXRestKey _key;
	private ERXRestRequestNode _rootNode;

	public ERXRestRequest(ERXRestContext context, ERXRestRequestNode rootNode, String requestPath) throws ERXRestException, ERXRestNotFoundException {
		_key = ERXRestKey.parse(context, rootNode, requestPath);
		_rootNode = rootNode;
	}
	
	/**
	 * Constructs a new REST request.
	 * 
	 * @param key the last key in the requested keypath
	 * @param rootNode the root node of the request document
	 */
	public ERXRestRequest(ERXRestKey key, ERXRestRequestNode rootNode) {
		_key = key;
		_rootNode = rootNode;
	}
	
	/**
	 * Returns the last key in the requested key path.
	 * 
	 * @return the last key in the requested key path
	 */
	public ERXRestKey key() {
		return _key;
	}

	/**
	 * Returns the root node of the request document.
	 * 
	 * @return the root node of the request document
	 */
	public ERXRestRequestNode rootNode() {
		return _rootNode;
	}
}
