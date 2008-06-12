package er.indexing.example.eof;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXMutableDictionary;

public class Asset extends _Asset {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Asset.class);

    public static final AssetClazz clazz = new AssetClazz();
    public static class AssetClazz extends _Asset._AssetClazz {
        /* more clazz methods here */
    }

    public interface Key extends _Asset.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }

    private static int key = 10000;
    
    private NSDictionary pk = new NSDictionary(new Integer(key++), "id"); 
    
    public NSDictionary primaryKeyDictionary(boolean inTransaction) {
        if(isNewObject()) {
            return pk;
        }
        return super.primaryKeyDictionary(inTransaction);
    }

    // global storage of custom attributes
    private NSMutableDictionary<String, String> _genericInfos = ERXMutableDictionary.synchronizedDictionary();
    
    public String genericInfo() {
    	return _genericInfos.objectForKey(primaryKeyInTransaction());
    }
    
    public void setGenericInfo(String value) {
    	_genericInfos.setObjectForKey(value, primaryKeyInTransaction());
    }
}
