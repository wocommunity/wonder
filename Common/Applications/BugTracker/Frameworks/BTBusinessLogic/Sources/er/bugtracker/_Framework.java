// _Framework.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Framework.java instead.
package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import java.util.*;
import java.math.BigDecimal;

public abstract class _Framework extends ERXGenericRecord {

    public _Framework() {
        super();
    }

    public static abstract class _FrameworkClazz extends er.extensions.ERXGenericRecord.ERXGenericRecordClazz {

        public NSArray orderedFrameworks(EOEditingContext ec) {
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, "Framework", "orderedFrameworks", null);
        }

    }


    public String name() {
        return (String)storedValueForKey("name");
    }
    public void setName(String aValue) {
        takeStoredValueForKey(aValue, "name");
    }

    public Number ordering() {
        return (Number)storedValueForKey("ordering");
    }
    public void setOrdering(Number aValue) {
        takeStoredValueForKey(aValue, "ordering");
    }

    public NSTimestamp ownedSince() {
        return (NSTimestamp)storedValueForKey("ownedSince");
    }
    public void setOwnedSince(NSTimestamp aValue) {
        takeStoredValueForKey(aValue, "ownedSince");
    }

    public er.bugtracker.People owner() {
        return (er.bugtracker.People)storedValueForKey("owner");
    }

    public void setOwner(er.bugtracker.People aValue) {
        takeStoredValueForKey(aValue, "owner");
    }
    public void addToBothSidesOfOwner(er.bugtracker.People object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "owner");
    }
    public void removeFromBothSidesOfOwner(er.bugtracker.People object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "owner");
    }

}
