package er.rest.format;

import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;
import er.rest.format.ERXRestFormat.Delegate;

/**
 * ERXFormRestParser is a rest parser that reads data from form values. 
 * 
 * @author mschrag
 */
public class ERXFormRestParser implements IERXRestParser {
	@Override
	public ERXRestRequestNode parseRestRequest(IERXRestRequest request, Delegate delegate, ERXRestContext context) {
		ERXRestRequestNode rootNode = new ERXRestRequestNode();
		for (String keyPath : request.keyNames()) {
			rootNode.takeValueForKeyPath(request.objectForKey(keyPath), keyPath);
		}
		return rootNode;
	}

}
