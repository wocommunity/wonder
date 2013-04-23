package er.rest.format;

import er.rest.ERXRestNameRegistry;
import er.rest.ERXRestRequestNode;

public class ERXEmberRestWriter extends ERXJSONRestWriter {
	@Override
	protected ERXRestRequestNode processNode(ERXRestRequestNode node) {
		
		ERXRestRequestNode rootNode = new ERXRestRequestNode(null, true);

		if(node.isArray()) {
			System.out.println("isArray");
			//ERXRestRequestNode recordsNode = new ERXRestRequestNode(ERXRestNameRegistry.registry().externalNameForInternalName(node.type()), false);
			
			ERXRestRequestNode recordsNode = new ERXRestRequestNode("lots", false);
			
			recordsNode.setArray(true);
			rootNode.addChild(recordsNode);

			
			for (ERXRestRequestNode child : node.children()) {
				recordsNode.addChild(child);
			}
		}
		else {		
			System.out.println("not array");
			rootNode.addChild(node);
		}

		
		return rootNode;
	}
}
