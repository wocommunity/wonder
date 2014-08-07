package er.rest.format;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.rest.ERXRestNameRegistry;
import er.rest.ERXRestRequestNode;

/**
 * ERXRestFormatDelegate is the default implementation of the ERXRestFormat.Delegate interface.
 * 
 * @property ERXRest.idKey (default "id") Override this property if you want to use a different key for the 'id' attribute
 * @property ERXRest.nilKey (default "nil") Override this property if you want to use a different key for the 'nil' attribute
 * @property ERXRest.writeNilKey (default "true") 
 * @property ERXRest.pluralEntityNames (default "true")
 * @property ERXRest.typeKey (default "type") Override this property if you want to use a different key for the 'type' attribute
 * @property ERXRest.writeTypeKey (default "true")
 *
 * @author mschrag
 */
public class ERXEmberFormatDelegate extends ERXRestFormatDelegate {
	public static final String ID_KEY = "id";
	public static final String TYPE_KEY = "type";
	public static final String NIL_KEY = "nil";

	private String _idKey;
	private String _typeKey;
	private String _nilKey;
	private boolean _arrayTypes;
	private boolean _writeNilKey;
	private boolean _pluralNames;
	private boolean _underscoreNames;
	private boolean _writeTypeKey;

	public ERXEmberFormatDelegate() {
		this(ERXProperties.stringForKeyWithDefault("ERXRest.idKey", ERXRestFormatDelegate.ID_KEY), ERXProperties.stringForKeyWithDefault("ERXRest.typeKey", ERXRestFormatDelegate.TYPE_KEY), ERXProperties.stringForKeyWithDefault("ERXRest.nilKey", ERXRestFormatDelegate.NIL_KEY), ERXProperties.booleanForKeyWithDefault("ERXRest.writeNilKey", true), ERXProperties.booleanForKeyWithDefault("ERXRest.pluralEntityNames", true), false, false);
	}

	public ERXEmberFormatDelegate(String idKey, String typeKey, String nilKey, boolean writeNilKey, boolean pluralNames, boolean underscoreNames, boolean arrayTypes) {
		this(idKey, typeKey, nilKey, writeNilKey, pluralNames, underscoreNames, arrayTypes, ERXProperties.booleanForKeyWithDefault("ERXRest.writeTypeKey", true));
	}
	
	public ERXEmberFormatDelegate(String idKey, String typeKey, String nilKey, boolean writeNilKey, boolean pluralNames, boolean underscoreNames, boolean arrayTypes, boolean writeTypeKey) {
		_idKey = idKey;
		_typeKey = typeKey;
		_nilKey = nilKey;
		_writeNilKey = writeNilKey;
		_pluralNames = pluralNames;
		_underscoreNames = underscoreNames;
		_arrayTypes = arrayTypes;
		_writeTypeKey = writeTypeKey;
	}

	public void nodeWillWrite(ERXRestRequestNode node) {
		if (node.isRootNode() && node.name() != null && !node.isArray()) {
			node.setName(ERXStringUtilities.uncapitalize(node.name()));
		}
		super.nodeWillWrite(node);
	}
}
