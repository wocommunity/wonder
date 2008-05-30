// _ERCPreference.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to ERCPreference.java instead.
package er.corebusinesslogic;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _ERCPreference extends ERCStampedEnterpriseObject {

    public _ERCPreference() {
        super();
    }

    public static abstract class _ERCPreferenceClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray preferencesWithKey(EOEditingContext ec, Object key) {
            NSMutableDictionary _dict = new NSMutableDictionary(2);
            
            if(key != null) _dict.setObjectForKey( key, "key");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "ERCPreference", "preferences", _dict);
        }

        public NSArray userPrefsWithKeyId(EOEditingContext ec, Object key, Object id) {
            NSMutableDictionary _dict = new NSMutableDictionary(2);
            
            if(key != null) _dict.setObjectForKey( key, "key");
            if(id != null) _dict.setObjectForKey( id, "id");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "ERCPreference", "userPrefs", _dict);
        }

    }


    public String key() {
        return (String)storedValueForKey("key");
    }
    public void setKey(String aValue) {
        takeStoredValueForKey(aValue, "key");
    }

    public String value() {
        return (String)storedValueForKey("value");
    }
    public void setValue(String aValue) {
        takeStoredValueForKey(aValue, "value");
    }

    public Number userID() {
        return (Number)storedValueForKey("userID");
    }
    public void setUserID(Number aValue) {
        takeStoredValueForKey(aValue, "userID");
    }
}
