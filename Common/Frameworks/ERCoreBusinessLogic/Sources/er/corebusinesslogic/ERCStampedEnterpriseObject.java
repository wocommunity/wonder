/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;

public abstract class ERCStampedEnterpriseObject extends ERCEnterpriseObject {

    private boolean _propagateWillChange=true;
    public abstract String relationshipNameForLogEntry();
    public abstract EOEnterpriseObject logEntryType();
    public EOEnterpriseObject insertionLogEntry=null;
    
    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
        setCreated(new NSTimestamp());
        // Shouldn't use touch() here as it can be overridden in subclasses.
        setLastModified(new NSTimestamp());
        if (relationshipNameForLogEntry()!=null && logEntryType() != null) {
            insertionLogEntry=ERCoreBusinessLogic.createLogEntryLinkedToEO(logEntryType(),
                                                                   null,
                                                                   this,
                                                                   relationshipNameForLogEntry());
        }
    }

    public void willInsert() {
        super.willInsert();
        touch();
    }

    public void willUpdate() {
        super.willUpdate();
        touch();
    }

    public void willDelete() {
        // this in theory should not have much effect
        // however EOF seems to have trouble with some cascade configuration
        // this will maybe help track them down
        super.willDelete();
        touch();
    }

    
    public void touch() { setLastModified(new NSTimestamp()); }
    
    public void addObjectToBothSidesOfLogEntryRelationshipWithKey(EOEnterpriseObject object,
                                                                  String key) {
        // if we said insertionLogEntry=null in validateForInsert, we run the risk of
        // the save failing and the user making a modif, which then would not be
        // propagated to the log entry.
        if (insertionLogEntry!=null && editingContext().insertedObjects().containsObject(this))
            insertionLogEntry.addObjectToBothSidesOfRelationshipWithKey(object,key);
    }

    public NSTimestamp created() { return (NSTimestamp)storedValueForKey("created"); }
    public void setCreated(NSTimestamp value) { takeStoredValueForKey(value, "created"); }

    public NSTimestamp lastModified() { return (NSTimestamp)storedValueForKey("lastModified"); }
    public void setLastModified(NSTimestamp value) { takeStoredValueForKey(value, "lastModified"); }
}
