// _ERCStatic.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to ERCStatic.java instead.
package er.corebusinesslogic;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _ERCStatic extends ERXGenericRecord {

    public static final String ENTITY = "ERCStatic";

    public interface Key  {
        public static final String VALUE = "value";
        public static final String KEY = "key";  
    }

    public static abstract class _ERCStaticClazz extends ERXGenericRecord.ERXGenericRecordClazz {
 

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
