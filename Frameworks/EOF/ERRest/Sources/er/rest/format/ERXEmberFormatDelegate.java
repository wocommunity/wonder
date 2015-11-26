package er.rest.format;

import er.extensions.foundation.ERXStringUtilities;
import er.rest.ERXRestRequestNode;

public class ERXEmberFormatDelegate extends ERXRestFormatDelegate {
	public ERXEmberFormatDelegate() {
		super();
	}
	
	public ERXEmberFormatDelegate(String idKey, String typeKey, String nilKey, boolean writeNilKey, boolean pluralNames, boolean underscoreNames, boolean arrayTypes) {
		super(idKey, typeKey, nilKey, writeNilKey, pluralNames, underscoreNames, arrayTypes);
	}

	public void nodeWillWrite(ERXRestRequestNode node) {
		// uncapitalize type wrapper
		if (node.isRootNode() && node.name() != null && !node.isArray()) {
			node.setName(ERXStringUtilities.uncapitalize(node.name()));
		}
		super.nodeWillWrite(node);
	}
}
