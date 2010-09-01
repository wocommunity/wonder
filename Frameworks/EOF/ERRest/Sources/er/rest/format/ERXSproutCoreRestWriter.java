package er.rest.format;

import java.util.Enumeration;

import er.rest.ERXRestRequestNode;

public class ERXSproutCoreRestWriter extends ERXJSONRestWriter {
	@Override
	protected ERXRestRequestNode processNode(ERXRestRequestNode node) {
		ERXRestRequestNode rootNode = new ERXRestRequestNode(null, true);

		ERXRestRequestNode recordsNode = new ERXRestRequestNode("records", false);
		recordsNode.setArray(true);
		rootNode.addChild(recordsNode);

		if (node.isArray()) {
			/*for (ERXRestRequestNode child : node.children()) {*/
			for (Enumeration childEnum = node.children().objectEnumerator(); childEnum.hasMoreElements(); ) {
				ERXRestRequestNode child = (ERXRestRequestNode)childEnum.nextElement();
				recordsNode.addChild(child);
			}
		}
		else {
			recordsNode.addChild(node);
		}

		ERXRestRequestNode idsNode = new ERXRestRequestNode("ids", false);
		idsNode.setArray(true);
		rootNode.addChild(idsNode);

		/*for (ERXRestRequestNode child : recordsNode.children()) {*/
		for (Enumeration childEnum = recordsNode.children().objectEnumerator(); childEnum.hasMoreElements(); ) {
			ERXRestRequestNode child = (ERXRestRequestNode)childEnum.nextElement();
			Object id = child.id();
			idsNode.addChild(new ERXRestRequestNode(null, id, false));
		}

		ERXRestRequestNode countNode = new ERXRestRequestNode("count", Integer.valueOf(recordsNode.children()./*size*/count()), false);
		rootNode.addChild(countNode);

		return rootNode;
	}
}
