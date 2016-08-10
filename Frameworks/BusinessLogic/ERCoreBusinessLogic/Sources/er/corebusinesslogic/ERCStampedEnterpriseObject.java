/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.ERXExtensions;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXRetainer;
import er.extensions.foundation.ERXSelectorUtilities;

/**
 * EO subclass that has a timestamp with its creation date, the most recent modification, 
 * and a log entry describing the change.
 *
 * @property er.corebusinesslogic.ERCStampedEnterpriseObject.touchReadOnlyEntities
 */
public abstract class ERCStampedEnterpriseObject extends ERXGenericRecord {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public interface Keys {
		public static final String CREATED = "created";
		public static final String LAST_MODIFIED = "lastModified";
	}

	public static abstract class ERCStampedEnterpriseObjectClazz extends ERXGenericRecord.ERXGenericRecordClazz {

	}
    
    private static final Logger log = LoggerFactory.getLogger(ERCStampedEnterpriseObject.class);

    public static String [] TimestampAttributeKeys = new String[] { Keys.CREATED, Keys.LAST_MODIFIED};
    
    private static final Map<EOEditingContext, NSTimestamp> _datesPerEC = Collections.synchronizedMap(new WeakHashMap<EOEditingContext, NSTimestamp>());

    public static class Observer {
        public void updateTimestampForEditingContext(NSNotification n) {
            NSTimestamp now=new NSTimestamp();
            EOEditingContext editingContext = (EOEditingContext)n.object();

            log.debug("Timestamp for {}: {}", editingContext, now);

            _datesPerEC.put(editingContext, now);
        }
    }


    protected static void initialize() {
        NSNotificationCenter center = NSNotificationCenter.defaultCenter();
        NSSelector sel = ERXSelectorUtilities.notificationSelector("updateTimestampForEditingContext");
        Observer observer = new Observer();
        ERXRetainer.retain(observer);
        center.addObserver(observer, sel, ERXExtensions.objectsWillChangeInEditingContext, null);
    }


    public EOEnterpriseObject insertionLogEntry=null;
    
    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
        if (this instanceof ERCLogEntryInterface) {
            ERCLogEntryInterface lei = (ERCLogEntryInterface) this;
            String relationshipName =lei.relationshipNameForLogEntry();
            EOEnterpriseObject logType = lei.logEntryType();
            if (relationshipName != null && logType != null) {
                insertionLogEntry=ERCLogEntry.clazz.createLogEntryLinkedToEO(logType, null, this, relationshipName);
            }
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

    @Override
    public void willInsert() {
        super.willInsert();
        touch();
        setCreated(lastModified());
    }

    @Override
    public void willUpdate() {
        super.willUpdate();
        touch();
    }

    @Override
    public void willDelete() {
        // this in theory should not have much effect
        // however EOF seems to have trouble with some cascade configuration
        // this will maybe help track them down
        super.willDelete();
        touch();
    }

    private static Boolean _touchReadOnlyEntities;
    
    /**
     * Returns whether or not read-only entities should be touched. This setting is only here in case there is a performance
     * issue introduced by looking up the entity() in touch(), so we can roll it back out.
     * 
     * @return whether or not read-only entities should be touched (defaults to false)
     */
    protected static boolean touchReadOnlyEntities() {
        if (_touchReadOnlyEntities == null) {
            // MS: don't worry about double-null check this-and-that .. it's a Boolean, so no constructor and worst case we double-check a property 
            _touchReadOnlyEntities = ERXProperties.booleanForKeyWithDefault("er.corebusinesslogic.ERCStampedEnterpriseObject.touchReadOnlyEntities", false);
        }
        return _touchReadOnlyEntities;
    }

    private void touch() {
        // MS: Don't touch read-only entities -- that's just rude.
        if (!ERCStampedEnterpriseObject.touchReadOnlyEntities()) {
            EOEntity entity = entity();
            if (entity != null && entity.isReadOnly()) {
                return;
            }
        }

        NSTimestamp date;
        EOEditingContext editingContext = editingContext();

        if (editingContext != null) {
            date = _datesPerEC.get(editingContext);
        } else {
            log.error("Null editingContext in touch() for: {}", this);
            date = null;
        }

        if (date == null) { //either because there was no EC, or because there was no value in the Map
            log.error("Null modification date found in touch() call - EC delegate is probably missing");
            date = new NSTimestamp();
        }
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

    public NSTimestamp created() { return (NSTimestamp)storedValueForKey(Keys.CREATED); }
    public void setCreated(NSTimestamp value) { takeStoredValueForKey(value, Keys.CREATED); }

    public NSTimestamp lastModified() { return (NSTimestamp)storedValueForKey(Keys.LAST_MODIFIED); }
    public void setLastModified(NSTimestamp value) { takeStoredValueForKey(value, Keys.LAST_MODIFIED); }
}
