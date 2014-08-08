package er.rest.format;

import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.rest.ERXRestRequestNode;

public class ERXEmberRestWriter extends ERXJSONRestWriter {
	protected ERXRestRequestNode processNode(ERXRestRequestNode node) {
		ERXRestRequestNode rootNode = new ERXRestRequestNode(null, true);
		String rootObjectName;
		NSMutableArray<ERXRestRequestNode> nodesToAdd = null;
		NSMutableArray<ERXRestRequestNode> nodesToRemove = null;
		ERXRestRequestNode linksNode = null;
		if (node.isArray()) {
			rootObjectName = ERXStringUtilities.uncapitalize(ERXLocalizer.englishLocalizer().plurifiedString(node.childAtIndex(0).type(), 2));
			ERXRestRequestNode recordsNode = new ERXRestRequestNode(rootObjectName, false);
			recordsNode.setArray(true);
			rootNode.addChild(recordsNode);
			if (rootObjectName == null) {
				rootObjectName = "undefined";
			}
			for (ERXRestRequestNode child : node.children()) {
				linksNode = new ERXRestRequestNode("links", false);
				nodesToAdd = new NSMutableArray<ERXRestRequestNode>();
				nodesToRemove = new NSMutableArray<ERXRestRequestNode>();
				recordsNode.addChild(child);
				for (ERXRestRequestNode subChild : child.children()) {
					if (subChild.isArray() ) {
						// Return link to the relationship instead of id array if there are over 300 objects or the resulting relationship fetch request will be too large for some browsers and servers
						if (subChild.children().size() > 300) {
							String url = "/" + rootObjectName + "/" + child.id() + "/" + subChild.name();
							linksNode.addChild(new ERXRestRequestNode(subChild.name(), url, false) );
							nodesToRemove.add(subChild);
						}
						else {
							// rewrite relationship to ember format of objects: [id, id] format instead of objects: { [id:1, id:2] } 
							if (subChild.children().size() > 0 && subChild.children().objectAtIndex(0).children().size() == 0) {
								ERXRestRequestNode newSubChild = new ERXRestRequestNode(subChild.name(), false);
								nodesToAdd.add(newSubChild);
								newSubChild.setArray(true);
								for (ERXRestRequestNode idNode : subChild.children()) {
									newSubChild.addChild(new ERXRestRequestNode(null, idNode.id(), false));
								}
								nodesToRemove.add(subChild);
							}
							else {
								// Multiple keys are included in the filter so we are keeping embedded objects in the relationship instead of ids
								for (ERXRestRequestNode idNode : subChild.children()) {
									this.processNode(idNode);
								}
							}
						}
					}
					else {
						if (subChild.id() != null) {
							// use id of the object if no keys were specified in the filter. object: id instead of object: {id:1}
							if (subChild.children().size() == 0) {
								ERXRestRequestNode newSubChild = new ERXRestRequestNode(subChild.name(), subChild.id(), false);
								nodesToAdd.add(newSubChild);
								nodesToRemove.add(subChild);
							}
							else {
								processNode(subChild);
							}
						}
					} 
				}
				child.children().removeAll(nodesToRemove);
				child.children().addAll(nodesToAdd);
				if(linksNode.children().size() > 0) {
					child.addChild(linksNode);
				}
			}
		}
		else {  
			rootNode.addChild(node);
			rootObjectName = ERXStringUtilities.uncapitalize(node.type());
			nodesToAdd = new NSMutableArray<ERXRestRequestNode>();
			nodesToRemove = new NSMutableArray<ERXRestRequestNode>();
			for (ERXRestRequestNode subChild : node.children()) {
				if (subChild.isArray() ) {
					// Return link to the relationship instead of id array if there are over 300 objects or the request will be too large for some browsers
					if (subChild.children().size() > 300) {
						nodesToRemove.add(subChild);
						String url = "/" + rootObjectName + "/" + node.id() + "/" + subChild.name();
						if (linksNode == null) {
							linksNode = new ERXRestRequestNode("links", false);
						}
						linksNode.addChild(new ERXRestRequestNode(subChild.name(), url, false) );
					}
					else {
						if (subChild.children().size() > 0 && subChild.children().objectAtIndex(0).children().size() == 0) {
							ERXRestRequestNode newSubChild = new ERXRestRequestNode(subChild.name(), false);
							nodesToAdd.add(newSubChild);
							newSubChild.setArray(true);
							for (ERXRestRequestNode idNode : subChild.children()) {
								newSubChild.addChild(new ERXRestRequestNode(null, idNode.id(), false));
							}
							nodesToRemove.add(subChild);
						}
						else {
							for (ERXRestRequestNode idNode : subChild.children()) {
								this.processNode(idNode);
							}
						}
					}
				}
				else {
					if (subChild.id() != null) {
						if (subChild.children().size() == 0) {
						 	ERXRestRequestNode newSubChild = new ERXRestRequestNode(subChild.name(), subChild.id(), false);
						 	nodesToAdd.add(newSubChild);
							nodesToRemove.add(subChild);
						}
						else {
							processNode(subChild);
						}
					}
				} 
			}
			node.children().removeAll(nodesToRemove);
			node.children().addAll(nodesToAdd);
			if (linksNode != null && linksNode.children().size() > 0) {
				node.addChild(linksNode);
			}
		}
		return rootNode;
	}
}
