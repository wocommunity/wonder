package er.rest.format;

import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;

public class ERXEmberRestParser extends ERXJSONRestParser {
	@Override
	public ERXRestRequestNode parseRestRequest(IERXRestRequest request, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		// unwrapping type node
		ERXRestRequestNode node = super.parseRestRequest(request, delegate, context);
		node = node.childAtIndex(0);
		return node;
	}
}