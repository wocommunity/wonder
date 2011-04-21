package er.rest.format;

import er.rest.ERXRestRequestNode;

public class ERXSproutCoreRestWriter extends ERXJSONRestWriter {
	@Override
	protected ERXRestRequestNode processNode(ERXRestRequestNode node) {
		ERXRestRequestNode rootNode = new ERXRestRequestNode(null, true);

		ERXRestRequestNode recordsNode = new ERXRestRequestNode("content", false);
		recordsNode.setArray(true);
		rootNode.addChild(recordsNode);

		if (node.isArray()) {
			for (ERXRestRequestNode child : node.children()) {
				recordsNode.addChild(child);
			}
		}
		else {
			recordsNode.addChild(node);
		}

		ERXRestRequestNode idsNode = new ERXRestRequestNode("ids", false);
		idsNode.setArray(true);
		rootNode.addChild(idsNode);

		for (ERXRestRequestNode child : recordsNode.children()) {
			Object id = child.id();
			idsNode.addChild(new ERXRestRequestNode(null, id, false));
		}

		ERXRestRequestNode countNode = new ERXRestRequestNode("count", Integer.valueOf(recordsNode.children().size()), false);
		rootNode.addChild(countNode);

		return rootNode;
	}
}
