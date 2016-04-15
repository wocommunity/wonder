package er.indexing.example.eof;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXMutableDictionary;
import er.indexing.attributes.ERIAttribute;
import er.indexing.attributes.ERIAttributeGroup;
import er.indexing.attributes.ERIDocument;
import er.indexing.attributes.ERIExtensibleObject;

public class Asset extends _Asset implements ERIExtensibleObject {
    public static final AssetClazz clazz = new AssetClazz();
    public static class AssetClazz extends _Asset._AssetClazz {
        /* more clazz methods here */
    }

    public interface Key extends _Asset.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }

    // global storage of custom attributes
    private NSMutableDictionary<String, String> _genericInfos = ERXMutableDictionary.synchronizedDictionary();
    
    public String genericInfo() {
    	return _genericInfos.objectForKey(primaryKeyInTransaction());
    }
    
    public void setGenericInfo(String value) {
    	_genericInfos.setObjectForKey(value, primaryKeyInTransaction());
    }

    public String attributeGroupName() {
        return "TestGroup";
    }
    
    private NSArray<ERIAttribute> attributes() {
        return attributeGroup().allAttributes();
    }

    private ERIAttributeGroup attributeGroup() {
        return ERIAttributeGroup.clazz.attributeGroupForName(editingContext(), attributeGroupName());
    }

    public ERIDocument document() {
        ERIDocument result = attributeGroup().documentForGlobalID(editingContext(), permanentGlobalID());
        return result;
    }
}
