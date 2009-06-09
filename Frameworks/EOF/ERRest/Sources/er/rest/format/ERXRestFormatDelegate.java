package er.rest.format;

import er.rest.ERXRestNameRegistry;
import er.rest.ERXRestRequestNode;

/**
 * ERXRestFormatDelegate is the default implementation of the ERXRestFormat.Delegate interface.
 * 
 * @author mschrag
 */
public class ERXRestFormatDelegate implements ERXRestFormat.Delegate {
	public static final String ID_KEY = "id";
	public static final String TYPE_KEY = "type";
	public static final String NIL_KEY = "nil";

	private String _idKey;
	private String _typeKey;
	private String _nilKey;
	private boolean _writeNilKey;

	public ERXRestFormatDelegate() {
		this(ERXRestFormatDelegate.ID_KEY, ERXRestFormatDelegate.TYPE_KEY, ERXRestFormatDelegate.NIL_KEY, true);
	}

	public ERXRestFormatDelegate(String idKey, String typeKey, String nilKey, boolean writeNilKey) {
		_idKey = idKey;
		_typeKey = typeKey;
		_nilKey = nilKey;
		_writeNilKey = writeNilKey;
	}

	public void nodeDidParse(ERXRestRequestNode node) {
		if (node.isRootNode()) {
			node.setName(ERXRestNameRegistry.registry().internalNameForExternalName(node.name()));
		}

		Object id = node.removeAttributeOrChildNodeNamed(_idKey);
		node.setID(id);

		String externalType = (String) node.removeAttributeOrChildNodeNamed(_typeKey);
		if (externalType != null) {
			node.setType(ERXRestNameRegistry.registry().internalNameForExternalName(externalType));
		}

		Object nil = node.removeAttributeOrChildNodeNamed(_nilKey);
		if (nil != null) {
			node.setNull("true".equals(nil) || Boolean.TRUE.equals(nil));
		}
	}

	public void nodeWillWrite(ERXRestRequestNode node) {
		if (node.isRootNode()) {
			node.setName(ERXRestNameRegistry.registry().externalNameForInternalName(node.name()));
		}

		Object id = node.id();
		if (id != null) {
			node.setAttributeForKey(String.valueOf(id), _idKey);
		}

		String internalType = node.type();
		if (internalType != null) {
			node.setAttributeForKey(ERXRestNameRegistry.registry().externalNameForInternalName(internalType), _typeKey);
		}

		if (node.isNull() && _writeNilKey) {
			node.setAttributeForKey("true", _nilKey);
		}
	}
}