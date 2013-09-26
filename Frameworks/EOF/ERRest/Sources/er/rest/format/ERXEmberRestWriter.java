package er.rest.format;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.rest.ERXRestNameRegistry;
import er.rest.ERXRestRequestNode;

public class ERXEmberRestWriter extends ERXJSONRestWriter {
	protected ERXRestRequestNode processNode(ERXRestRequestNode node) {
		System.out.println("running " + node.name());
		ERXRestRequestNode rootNode = new ERXRestRequestNode(null, true);
		ERXRestRequestNode recordsNode = new ERXRestRequestNode(ERXStringUtilities.uncapitalize( ERXRestNameRegistry.registry().externalNameForInternalName( ERXLocalizer.englishLocalizer().plurifiedString(node.childAtIndex(0).type(), 2))), false);
		if(node.isArray()) {
			recordsNode.setArray(true);
			rootNode.addChild(recordsNode);
			for (ERXRestRequestNode child : node.children()) {
				System.out.println("child:" + child.name());
				recordsNode.addChild(child);
			}
			
			// relationships:
			/*
			ERXRestRequestNode idsNode = new ERXRestRequestNode("links", false);
			idsNode.setArray(true);
			recordsNode.addChild(idsNode);
			
			
			for (ERXRestRequestNode child : recordsNode.children()) {
				Object id = child.id();
				idsNode.addChild(new ERXRestRequestNode(null, id, false));
			}

			ERXRestRequestNode countNode = new ERXRestRequestNode("count", Integer.valueOf(recordsNode.children().size()), false);
			rootNode.addChild(countNode);
			*/
			
		}
		else {
			rootNode.addChild(node);
			System.out.println("single node");
		}
		return rootNode;
	}
}
