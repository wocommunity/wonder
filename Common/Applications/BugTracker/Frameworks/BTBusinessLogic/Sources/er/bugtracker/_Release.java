// _Release.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Release.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _Release extends ERXGenericRecord {

    public _Release() {
        super();
    }

    public static abstract class _ReleaseClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray fetchAll(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Release", "FetchAll", null);
        }

    }


    public String name() {
        return (String)storedValueForKey("name");
    }
    public void setName(String aValue) {
        takeStoredValueForKey(aValue, "name");
    }

    public Number isOpen() {
        return (Number)storedValueForKey("isOpen");
    }
    public void setIsOpen(Number aValue) {
        takeStoredValueForKey(aValue, "isOpen");
    }
}
