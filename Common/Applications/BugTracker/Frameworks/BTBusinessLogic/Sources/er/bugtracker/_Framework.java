// _Framework.java
// 
// Created by eogenerator
// DO NOT EDIT.  Make changes to Framework.java instead.
package er.bugtracker;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.eof.ERXGenericRecord;

public abstract class _Framework extends ERXGenericRecord {

    public _Framework() {
        super();
    }

    public static abstract class _FrameworkClazz extends ERXGenericRecord.ERXGenericRecordClazz {

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

    public People owner() {
        return (People)storedValueForKey("owner");
    }

    public void setOwner(People aValue) {
        takeStoredValueForKey(aValue, "owner");
    }
    public void addToBothSidesOfOwner(People object) {
        addObjectToBothSidesOfRelationshipWithKey(object, "owner");
    }
    public void removeFromBothSidesOfOwner(People object) {
        removeObjectFromBothSidesOfRelationshipWithKey(object, "owner");
    }

}
