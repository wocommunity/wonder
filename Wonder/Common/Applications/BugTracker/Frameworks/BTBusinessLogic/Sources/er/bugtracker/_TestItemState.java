// _TestItemState.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to TestItemState.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _TestItemState extends ERXGenericRecord {

    public _TestItemState() {
        super();
    }

    public static abstract class _TestItemStateClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray fetchAll(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "TestItemState", "FetchAll", null);
        }

    }


    public String name() {
        return (String)storedValueForKey("name");
    }
    public void setName(String aValue) {
        takeStoredValueForKey(aValue, "name");
    }

    public Number sortOrder() {
        return (Number)storedValueForKey("sortOrder");
    }
    public void setSortOrder(Number aValue) {
        takeStoredValueForKey(aValue, "sortOrder");
    }
}
