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
import er.extensions.*;
import org.apache.log4j.*;

public abstract class ERCStampedEnterpriseObject extends ERCEnterpriseObject {

    private boolean _propagateWillChange=true;
    public abstract String relationshipNameForLogEntry();
    public abstract EOEnterpriseObject logEntryType();
    public EOEnterpriseObject insertionLogEntry=null;

    public static Category cat=Category.getInstance(ERCStampedEnterpriseObject.class);


    
    // FIXME not thread safe..
    private static NSMutableDictionary _datesPerEcID=new NSMutableDictionary();
    private static class _Touch {
        public void touch(NSNotification n) {
            NSTimestamp now=new NSTimestamp();
            if (cat.isDebugEnabled()) cat.debug("TimeStamp for "+n.object()+": now");
            _datesPerEcID.setObjectForKey(ERXConstant.integerForInt(System.identityHashCode(n.object())), now);
        }
    }
    
    static {
        NSNotificationCenter center = NSNotificationCenter.defaultCenter();
        center.addObserver(new _Touch(),
                           new NSSelector("touch",  ERXConstant.NotificationClassArray),
                           ERXExtensions.objectsWillChangeInEditingContext,
                           null);
    }        


    
    public void awakeFromInsertion(EOEditingContext ec) {
        super.awakeFromInsertion(ec);
        if (relationshipNameForLogEntry()!=null && logEntryType() != null) {
            insertionLogEntry=ERCoreBusinessLogic.createLogEntryLinkedToEO(logEntryType(),
                                                                   null,
                                                                   this,
                                                                   relationshipNameForLogEntry());
        }
        // We now set the date created/last modified in willInsert/Update/Delete
        // A side effect of this technique is that for new EOs, created/lastModified is null until the EO actually gets saved
        // which means it'll fail the validation
        // two options: either we poke a value in those attributes here (even though it will be modified in willInsert,
        // or we make the property keys not mandatory
        // I am option for the former.
        NSTimestamp t=new NSTimestamp();
        setCreated(t);
        setLastModified(t);
    }

    public void willInsert() {
        super.willInsert();
        touch();
        setCreated(lastModified());
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

    
    private void touch() {
        Number n=ERXConstant.integerForInt(System.identityHashCode(editingContext()));
        NSTimestamp date=(NSTimestamp)_datesPerEcID.objectForKey(n);
        if (date==null) throw new RuntimeException("Null modification date found in touch() call - EC delegate is probably missing");
        setLastModified(date);
    }
    
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
