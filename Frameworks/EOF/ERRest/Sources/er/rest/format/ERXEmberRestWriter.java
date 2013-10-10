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
		if(node.isArray()) {
			rootObjectName = ERXStringUtilities.uncapitalize( ERXRestNameRegistry.registry().externalNameForInternalName( ERXLocalizer.englishLocalizer().plurifiedString(node.childAtIndex(0).type(), 2)));
			ERXRestRequestNode recordsNode = new ERXRestRequestNode(rootObjectName, false);
			recordsNode.setArray(true);
			rootNode.addChild(recordsNode);
			if(rootObjectName == null) {
				System.out.println("!!!!!!!! null key setting to aaa");
				rootObjectName = "aaa";
			}
			for (ERXRestRequestNode child : node.children()) {
				//links 
				ERXRestRequestNode linksNode = new ERXRestRequestNode("links", false);
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
								Object id = idNode.id();
								newSubChild.addChild(new ERXRestRequestNode(null, id, false));
							}
							nodesToRemove.add(subChild);
						}
					} else {
						if(subChild.id() != null) {
						 	ERXRestRequestNode newSubChild = new ERXRestRequestNode(subChild.name(), subChild.id(), false);
						 	nodesToAdd.add(newSubChild);
						 	//Object id = subChild.id();
							//newSubChild.addChild(new ERXRestRequestNode(null, id, false));
							nodesToRemove.add(subChild);
						}
					} 
				}
				if(linksNode.children().size() > 0) {
					child.addChild(linksNode);
				}
				//remove nodes
				for(ERXRestRequestNode nodeToRemove : nodesToRemove) {
					child.removeChildNamed(nodeToRemove.name());
				}
				// add
				for(ERXRestRequestNode nodeToAdd : nodesToAdd) {
					child.addChild(nodeToAdd);
				}
			}
		}
		else {  
			rootNode.addChild(node);
			rootObjectName = ERXStringUtilities.uncapitalize(node.type());
			ERXRestRequestNode linksNode = new ERXRestRequestNode("links", false);
			nodesToAdd = new NSMutableArray<ERXRestRequestNode>();
			nodesToRemove = new NSMutableArray<ERXRestRequestNode>();
			for(ERXRestRequestNode subChild : node.children()) {
				if(subChild.isArray() ) {
					if(subChild.children().size() > 300) {
						nodesToRemove.add(subChild);
						String url = "/" + rootObjectName + "/" + node.id() + "/" + subChild.name();
						linksNode.addChild(new ERXRestRequestNode(subChild.name(), url, false) );
					}
					else {
						// save ids in a new array
						ERXRestRequestNode newSubChild = new ERXRestRequestNode(subChild.name(), false);
						nodesToAdd.add(newSubChild);
						newSubChild.setArray(true);
						for(ERXRestRequestNode idNode : subChild.children()) {
							Object id = idNode.id();
							newSubChild.addChild(new ERXRestRequestNode(null, id, false));
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
			//remove nodes
			for(ERXRestRequestNode nodeToRemove : nodesToRemove) {
				node.removeChildNamed(nodeToRemove.name());
			}
			// add
			for(ERXRestRequestNode nodeToAdd : nodesToAdd) {
				node.addChild(nodeToAdd);
			}
			if(linksNode.children().size() > 0) {
				node.addChild(linksNode);
			}
		}
		return rootNode;
	}
}
