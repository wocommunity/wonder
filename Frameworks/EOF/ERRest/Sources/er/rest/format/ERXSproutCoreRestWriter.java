package er.rest.format;

import er.rest.ERXRestRequestNode;

public class ERXSproutCoreRestWriter extends ERXJSONRestWriter {
	@Override
	protected ERXRestRequestNode processNode(ERXRestRequestNode node) {
		ERXRestRequestNode resultsDictNode = new ERXRestRequestNode(null, true);
		if (node.isArray()) {
			node.setName("results");
			resultsDictNode.addChild(node);
		}
		else {
			ERXRestRequestNode resultsArrayNode = new ERXRestRequestNode("results", false);
			resultsArrayNode.setArray(true);
			resultsArrayNode.addChild(node);
			resultsDictNode.addChild(resultsArrayNode);
		}
		return resultsDictNode;
	}
}
