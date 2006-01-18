// _ERCStatic.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to ERCStatic.java instead.
package er.corebusinesslogic;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.*;

public abstract class _ERCStatic extends ERXGenericRecord {

    public _ERCStatic() {
        super();
    }

    public static abstract class _ERCStaticClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray preferencesWithKey(EOEditingContext ec, Object key) {
            NSMutableDictionary _dict = new NSMutableDictionary(1);
            
            if(key != null) _dict.setObjectForKey( key, "key");
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "ERCStatic", "preferences", _dict);
        }

    }


    public String value() {
        return (String)storedValueForKey("value");
    }
    public void setValue(String aValue) {
        takeStoredValueForKey(aValue, "value");
    }

    public String key() {
        return (String)storedValueForKey("key");
    }
    public void setKey(String aValue) {
        takeStoredValueForKey(aValue, "key");
    }
}
