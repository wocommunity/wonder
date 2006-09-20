// _ERCHelpText.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to ERCHelpText.java instead.
package er.corebusinesslogic;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXGenericRecord;

public abstract class _ERCHelpText extends ERXGenericRecord {

    public interface Key  {
        public static final String VALUE = "value";
        public static final String KEY = "key";  
    }

    public static abstract class _ERCHelpTextClazz extends ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray objectsForKey(EOEditingContext context, String keyBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("Key", "ERCHelpText");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (keyBinding != null)
                bindings.setObjectForKey(keyBinding, "key");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

    }


    public String key() {
        return (String)storedValueForKey(Key.KEY);
    }
    public void setKey(String aValue) {
        takeStoredValueForKey(aValue, Key.KEY);
    }

    public String value() {
        return (String)storedValueForKey(Key.VALUE);
    }
    public void setValue(String aValue) {
        takeStoredValueForKey(aValue, Key.VALUE);
    }
}
