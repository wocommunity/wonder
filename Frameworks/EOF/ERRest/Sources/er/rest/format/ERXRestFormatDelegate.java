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
public class ERXRestFormatDelegate implements ERXRestFormat.Delegate {
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

	public ERXRestFormatDelegate() {
		this(ERXProperties.stringForKeyWithDefault("ERXRest.idKey", ERXRestFormatDelegate.ID_KEY), ERXProperties.stringForKeyWithDefault("ERXRest.typeKey", ERXRestFormatDelegate.TYPE_KEY), ERXProperties.stringForKeyWithDefault("ERXRest.nilKey", ERXRestFormatDelegate.NIL_KEY), ERXProperties.booleanForKeyWithDefault("ERXRest.writeNilKey", true), ERXProperties.booleanForKeyWithDefault("ERXRest.pluralEntityNames", true), false, false);
	}

	public ERXRestFormatDelegate(String idKey, String typeKey, String nilKey, boolean writeNilKey, boolean pluralNames, boolean underscoreNames, boolean arrayTypes) {
		this(idKey, typeKey, nilKey, writeNilKey, pluralNames, underscoreNames, arrayTypes, ERXProperties.booleanForKeyWithDefault("ERXRest.writeTypeKey", true));
	}
	
	public ERXRestFormatDelegate(String idKey, String typeKey, String nilKey, boolean writeNilKey, boolean pluralNames, boolean underscoreNames, boolean arrayTypes, boolean writeTypeKey) {
		_idKey = idKey;
		_typeKey = typeKey;
		_nilKey = nilKey;
		_writeNilKey = writeNilKey;
		_pluralNames = pluralNames;
		_underscoreNames = underscoreNames;
		_arrayTypes = arrayTypes;
		_writeTypeKey = writeTypeKey;
	}

	@Override
	public void nodeDidParse(ERXRestRequestNode node) {
		if (node.isRootNode()) {
			node.setName(ERXRestNameRegistry.registry().internalNameForExternalName(node.name()));
		}

		Object id = node.removeAttributeOrChildNodeNamed(_idKey);
		node.setID(id);

		String externalType = (String) node.removeAttributeOrChildNodeNamed(_typeKey);
		if (externalType != null) {
			if (_arrayTypes && "array".equals(externalType)) {
				node.setArray(true);
			}
			else {
				String type = ERXRestNameRegistry.registry().internalNameForExternalName(externalType);
				if (_underscoreNames) {
					type = ERXStringUtilities.underscoreToCamelCase(type, true);
				}
				node.setType(type);
			}
		}

		Object nil = node.removeAttributeOrChildNodeNamed(_nilKey);
		if (nil != null) {
			node.setNull("true".equals(nil) || Boolean.TRUE.equals(nil));
		}
		
		if (_underscoreNames) {
			String name = node.name();
			if (name != null) {
				name = ERXStringUtilities.underscoreToCamelCase(name, node.isRootNode());
				node.setName(name);
			}
		}
	}

	@Override
	public void nodeWillWrite(ERXRestRequestNode node) {
		if (node.isRootNode() && node.isArray()) {
			if (_pluralNames) {
				node.setName(ERXRestNameRegistry.registry().externalNameForInternalName(ERXLocalizer.englishLocalizer().plurifiedString(node.name(), 2)));
			}
			else {
				node.setName(ERXRestNameRegistry.registry().externalNameForInternalName(node.name()));
			}
		}

		Object id = node.id();
		if (id != null) {
			node.setAttributeForKey(id, _idKey);
		}

		if (_writeTypeKey) {
			String internalType = node.type();
			if (internalType != null) {
				if (_arrayTypes && node.isArray()) {
					node.setAttributeForKey("array", _typeKey);
				}
				else {
					String type = ERXRestNameRegistry.registry().externalNameForInternalName(internalType);
					if (_underscoreNames) {
						type = ERXStringUtilities.camelCaseToUnderscore(type, true);
					}
					node.setAttributeForKey(type, _typeKey);
				}
			}
		}

		if (node.isNull() && _writeNilKey) {
			node.setAttributeForKey(Boolean.TRUE, _nilKey);
		}
		
		if (_underscoreNames) {
			String name = node.name();
			if (name != null) {
				name = ERXStringUtilities.camelCaseToUnderscore(name, true);
				node.setName(name);
			}
		}
	}
}
