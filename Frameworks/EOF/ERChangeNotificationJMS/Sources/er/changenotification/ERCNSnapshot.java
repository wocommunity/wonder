//
// ERCNSnapshot.java
// Project ERChangeNotificationJMS
//
// Created by tatsuya on Sat Aug 31 2002
//
package er.changenotification;

import java.io.Serializable;
import java.util.Enumeration;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;

/** 
 * ERCNSnapshot encapsulates changes in enterprise objects for single 
 * saveChanges operation. It implements java.io.Serializable interface so that 
 * it can be transmitted between application instances as a JMS object message. 
 * <p>
 * Its constructor is called by ERCNPublisher object. It processes the 
 * change notification posted by the default EOObjectStoreCoordinator and 
 * populate the dictionaries with the snapshots of updated enterprise objects. 
 */
public class ERCNSnapshot implements Serializable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static final String INSERTED = "inserted";
    public static final String UPDATED = "updated";
    public static final String DELETED = "deleted";

    private final String _senderHost;
    private final Number _senderPort;
    private final String _senderAppName;

    private final NSDictionary _shapshotsForInsertionGroupedByEntity;
    private final NSDictionary _shapshotsForUpdateGroupedByEntity;
    private final NSDictionary _globalIDsForDeletionGroupedByEntity;
    
    private transient String _toString;
    private transient int _entryCount = 0;

    public ERCNSnapshot(NSNotification notification) {
        WOApplication app = WOApplication.application();
        _senderHost = app.host();
        _senderPort = app.port(); // Don't forget to apply Max's change
        _senderAppName = app.name();

        NSDictionary userInfo = notification.userInfo();

        ERCNConfiguration configuration = ERCNNotificationCoordinator.coordinator().configuration();
        if (configuration.changeTypesToPublish().containsObject(INSERTED))
            _shapshotsForInsertionGroupedByEntity = snapshotsGroupedByEntity((NSArray)userInfo.objectForKey("inserted"));
        else 
            _shapshotsForInsertionGroupedByEntity = NSDictionary.EmptyDictionary;

        if (configuration.changeTypesToPublish().containsObject(UPDATED))
            _shapshotsForUpdateGroupedByEntity = snapshotsGroupedByEntity((NSArray)userInfo.objectForKey("updated"));
        else 
            _shapshotsForUpdateGroupedByEntity = NSDictionary.EmptyDictionary;

        if (configuration.changeTypesToPublish().containsObject(DELETED))
            _globalIDsForDeletionGroupedByEntity = globalIDsGroupedByEntity((NSArray)userInfo.objectForKey("deleted"));
        else 
            _globalIDsForDeletionGroupedByEntity = NSDictionary.EmptyDictionary;

    }

    public NSDictionary shapshotsForInsertionGroupedByEntity() {
        return _shapshotsForInsertionGroupedByEntity;
    }
    
    public NSDictionary shapshotsForUpdateGroupedByEntity() {
        return _shapshotsForUpdateGroupedByEntity;
    }
    
    public NSDictionary globalIDsForDeletionGroupedByEntity() {
        return _globalIDsForDeletionGroupedByEntity;
    }

    public boolean shouldPostChange() { 
        return _entryCount > 0;
    }

    public static boolean shouldApplyChangeFor(String operation) {
        ERCNConfiguration configuration = ERCNNotificationCoordinator.coordinator().configuration();
        return configuration.changeTypesToSubscribe().containsObject(operation);
    }

    public static boolean shouldSynchronizeEntity(String entityName) {
        ERCNConfiguration configuration = ERCNNotificationCoordinator.coordinator().configuration();
        return ! configuration.entitiesNotToSynchronize().containsObject(entityName);
    }

    public String senderHost() {
        return _senderHost;
    }
    
    public Number senderPort() {
        return _senderPort;
    }
    
    public String senderAppName() {
        return _senderAppName;
    }

    public NSDictionary snapshotsGroupedByEntity(NSArray objects) {
        if (objects == null)  return NSDictionary.EmptyDictionary;
    
        NSMutableDictionary result = new NSMutableDictionary();
        EOEditingContext ec = new EOEditingContext();
        ec.lock();
        
        Enumeration e = objects.objectEnumerator();
        while (e.hasMoreElements()) {
            EOKeyGlobalID globalID = (EOKeyGlobalID) e.nextElement();
            String entityName = globalID.entityName();

            if (shouldSynchronizeEntity(entityName)) {
                EODatabaseContext dbContext = ERCNNotificationCoordinator.databaseContextForEntityNamed(entityName, ec);
                NSMutableArray snapshotsForEntity = (NSMutableArray)result.objectForKey(entityName);
                if (snapshotsForEntity == null) {
                    snapshotsForEntity = new NSMutableArray();
                    result.setObjectForKey(snapshotsForEntity, entityName);
                }
                NSDictionary snapshot = dbContext.snapshotForGlobalID(globalID);
                if (snapshot != null) {
                    snapshotsForEntity.addObject(snapshot);
                    _entryCount++;
                }
            }
        }
        ec.unlock();
        return result.immutableClone();
    }

    public NSDictionary globalIDsGroupedByEntity(NSArray objects) {
        if (objects == null)  return NSDictionary.EmptyDictionary;
    
        NSMutableDictionary result = new NSMutableDictionary();
        
        Enumeration e = objects.objectEnumerator();
        while (e.hasMoreElements()) {
            EOKeyGlobalID globalID = (EOKeyGlobalID) e.nextElement();
            String entityName = globalID.entityName();

            if (shouldSynchronizeEntity(entityName)) {
                NSMutableArray globalIDsForEntity = (NSMutableArray)result.objectForKey(entityName);
                if (globalIDsForEntity == null) {
                    globalIDsForEntity = new NSMutableArray();
                    result.setObjectForKey(globalIDsForEntity, entityName);
                }
                globalIDsForEntity.addObject(globalID);
                _entryCount++;
            }
        }
        return result.immutableClone();
    }

    @Override
    public String toString() {
        if (_toString == null) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append('<').append(getClass().getName()).append('\n');
            
            sbuf.append(" sender: ").append(senderHost()).append(':')
                .append(senderPort()).append('/').append(senderAppName()).append('\n');
            
            sbuf.append(" insertion: ").append(_summaryForChangeType(_shapshotsForInsertionGroupedByEntity));
            sbuf.append(" update: ").append(_summaryForChangeType(_shapshotsForUpdateGroupedByEntity));
            sbuf.append(" deletion: ").append(_summaryForChangeType(_globalIDsForDeletionGroupedByEntity));
            
            sbuf.append('>');
            _toString = sbuf.toString();
        }
        return _toString;
    }
    
    private String _summaryForChangeType(NSDictionary objectsGroupedByEntity) {
        StringBuilder sbuf = new StringBuilder();
        if (objectsGroupedByEntity.allKeys().count() == 0) {
            sbuf.append("none \n");
        } else {
            sbuf.append('\n');
            Enumeration entityNames = objectsGroupedByEntity.keyEnumerator();
            while (entityNames.hasMoreElements()) {
                String entityName = (String)entityNames.nextElement();
                sbuf.append("    ").append(entityName).append(": ");
                NSArray objects = (NSArray)objectsGroupedByEntity.objectForKey(entityName);
                sbuf.append(objects.count()).append(" objects \n");
            }
        }
        return sbuf.toString();
    }
    
}
