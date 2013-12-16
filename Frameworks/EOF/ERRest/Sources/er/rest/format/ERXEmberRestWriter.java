package er.rest.format;

import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.rest.ERXRestNameRegistry;
import er.rest.ERXRestRequestNode;

public class ERXEmberRestWriter extends ERXJSONRestWriter {
	protected ERXRestRequestNode processNode(ERXRestRequestNode node) {
		ERXRestRequestNode rootNode = new ERXRestRequestNode(null, true);
		String rootObjectName;
		NSMutableArray<ERXRestRequestNode> nodesToAdd = null;
		NSMutableArray<ERXRestRequestNode> nodesToRemove = null;
		ERXRestRequestNode linksNode = null;
		if(node.isArray()) {
			//rootObjectName = ERXStringUtilities.uncapitalize( ERXRestNameRegistry.registry().externalNameForInternalName( ERXLocalizer.englishLocalizer().plurifiedString(node.childAtIndex(0).type(), 2)));
			rootObjectName = ERXStringUtilities.uncapitalize( ERXLocalizer.englishLocalizer().plurifiedString(node.childAtIndex(0).type(), 2));
			ERXRestRequestNode recordsNode = new ERXRestRequestNode(rootObjectName, false);
			recordsNode.setArray(true);
			rootNode.addChild(recordsNode);
			if(rootObjectName == null) {
				System.out.println("!!!!!!!! null key setting to aaa");
				rootObjectName = "aaa";
			}
			for(ERXRestRequestNode child : node.children()) {
				//links 
				linksNode = new ERXRestRequestNode("links", false);
				nodesToAdd = new NSMutableArray<ERXRestRequestNode>();
				nodesToRemove = new NSMutableArray<ERXRestRequestNode>();
				//linksNode.setArray(true);
				recordsNode.addChild(child);
				for(ERXRestRequestNode subChild : child.children()) {
					if(subChild.isArray() ) {
						if(subChild.children().size() > 300) {
							String url = "/" + rootObjectName + "/" + child.id() + "/" + subChild.name();
							linksNode.addChild(new ERXRestRequestNode(subChild.name(), url, false) );
							nodesToRemove.add(subChild);
						}
						else {
							// save ids in a new array
							ERXRestRequestNode newSubChild = new ERXRestRequestNode(subChild.name(), false);
							nodesToAdd.add(newSubChild);
							newSubChild.setArray(true);
							for(ERXRestRequestNode idNode : subChild.children()) {
								newSubChild.addChild(new ERXRestRequestNode(null, idNode.id(), false));
							}
							nodesToRemove.add(subChild);
						}
					} else {
						if(subChild.id() != null) {
						 	ERXRestRequestNode newSubChild = new ERXRestRequestNode(subChild.name(), subChild.id(), false);
						 	nodesToAdd.add(newSubChild);
							nodesToRemove.add(subChild);
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
			for(ERXRestRequestNode subChild : node.children()) {
				if(subChild.isArray() ) {
					if(subChild.children().size() > 300) {
						nodesToRemove.add(subChild);
						String url = "/" + rootObjectName + "/" + node.id() + "/" + subChild.name();
						if(linksNode == null) {
							linksNode = new ERXRestRequestNode("links", false);
						}
						linksNode.addChild(new ERXRestRequestNode(subChild.name(), url, false) );
					}
					else {
						// save ids in a new array
						ERXRestRequestNode newSubChild = new ERXRestRequestNode(subChild.name(), false);
						nodesToAdd.add(newSubChild);
						newSubChild.setArray(true);
						for(ERXRestRequestNode idNode : subChild.children()) {
							newSubChild.addChild(new ERXRestRequestNode(null, idNode.id(), false));
						}
						nodesToRemove.add(subChild);
					}
				}
				else {
					if(subChild.id() != null) {
					 	ERXRestRequestNode newSubChild = new ERXRestRequestNode(subChild.name(), subChild.id(), false);
					 	nodesToAdd.add(newSubChild);
						nodesToRemove.add(subChild);
					}
				} 
			}
			node.children().removeAll(nodesToRemove);
			node.children().addAll(nodesToAdd);
			if(linksNode != null && linksNode.children().size() > 0) {
				node.addChild(linksNode);
			}
		}
		return rootNode;
	}
}
