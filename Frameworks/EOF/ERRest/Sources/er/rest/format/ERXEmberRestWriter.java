package er.rest.format;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.rest.ERXRestNameRegistry;
import er.rest.ERXRestRequestNode;

public class ERXEmberRestWriter extends ERXJSONRestWriter {
	protected ERXRestRequestNode processNode(ERXRestRequestNode node) {
		ERXRestRequestNode rootNode = new ERXRestRequestNode(null, true);
		if(node.isArray()) {
			ERXRestRequestNode recordsNode = new ERXRestRequestNode(ERXStringUtilities.uncapitalize( ERXRestNameRegistry.registry().externalNameForInternalName( ERXLocalizer.englishLocalizer().plurifiedString(node.childAtIndex(0).type(), 2))), false);
			recordsNode.setArray(true);
			rootNode.addChild(recordsNode);
			for (ERXRestRequestNode child : node.children()) {
				recordsNode.addChild(child);
			}
		}
		else {		
			rootNode.addChild(node);
		}
		return rootNode;
	}
}
