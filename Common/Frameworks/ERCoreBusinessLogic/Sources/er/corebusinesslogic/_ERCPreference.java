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

public abstract class _ERCPreference extends ERXGenericRecord {

    public static final String ENTITY = "ERCPreference";

    public interface Key  {
        public static final String VALUE = "value";
        public static final String USER_ID = "userID";
        public static final String KEY = "key";  
    }

    public static abstract class _ERCPreferenceClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

        public NSArray objectsForPreferences(EOEditingContext context, String keyBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("preferences", "ERCPreference");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (keyBinding != null)
                bindings.setObjectForKey(keyBinding, "key");
            spec = spec.fetchSpecificationWithQualifierBindings(bindings);

            return context.objectsWithFetchSpecification(spec);
        }

        public NSArray objectsForUserPrefs(EOEditingContext context, Number idBinding, String keyBinding) {
            EOFetchSpecification spec = EOFetchSpecification.fetchSpecificationNamed("userPrefs", "ERCPreference");

            NSMutableDictionary bindings = new NSMutableDictionary();

            if (keyBinding != null)
                bindings.setObjectForKey(keyBinding, "key");
            if (idBinding != null)
                bindings.setObjectForKey(idBinding, "id");
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

    public Number userID() {
        return (Number)storedValueForKey(Key.USER_ID);
    }
    public void setUserID(Number aValue) {
        takeStoredValueForKey(aValue, Key.USER_ID);
    }

    public String value() {
        return (String)storedValueForKey(Key.VALUE);
    }
    public void setValue(String aValue) {
        takeStoredValueForKey(aValue, Key.VALUE);
    }
}
