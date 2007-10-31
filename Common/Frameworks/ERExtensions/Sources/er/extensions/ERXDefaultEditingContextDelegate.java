/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import java.util.*;

/**
 * Default editing context delegate. This delegate
 * augments the regular transaction process by adding
 * the calling of willInsert, willUpdate or willDelete
 * on enterprise objects that are of type ERXGenericRecord
 * after saveChanges is called on the editing context, but
 * before validateForSave is called on the object. These
 * methods can give the object a last chance to modify itself
 * before validation occurs. The second enhancement is a built
 * in flushing of caches on subclasses of ERXGenericRecords
 * when objects have changes merged in or are invalidated.
 * Being able to maintain caches on enterprise objects that
 * are flushed when the underlying values change can be very
 * handy.
 */
public class ERXDefaultEditingContextDelegate extends ERXEditingContextDelegate {

    //	===========================================================================
    //	Class Constant(s)
    //	---------------------------------------------------------------------------    
    
    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXDefaultEditingContextDelegate.class);
    
    /** logging support for modified objects */
    public static final ERXLogger logMod = ERXLogger.getERXLogger("er.transaction.delegate.EREditingContextDelegate.modifedObjects");

    //	===========================================================================
    //	Class Method(s)
    //	---------------------------------------------------------------------------    

    /**
     * Returns a string of a verbose look a the given
     * enterprise object. Also includes the primary key.
     * @param eo enterprise object to create the debug string
     * 		for.
     * @return verbose description of the object.
     */
    // MOVEME: If this is usedful then it might be worth putting in EOEnterpriseObjectClazz or ERXEOFUtilities
    private static String toDebugString(EOEnterpriseObject eo) {
        String result=null;
        if (eo!=null) {
            if (eo instanceof ERXGenericRecord) {
                ERXGenericRecord rec = (ERXGenericRecord)eo;
                result="PKey: " + rec.primaryKey() + " - " + rec.toLongString();
            } else {
                result="Pkey: "+EOUtilities.primaryKeyForObject(eo.editingContext(),eo)+" - "+eo;
            }
        }
        return result;
    }    
    
    //	===========================================================================
    //	Instance Constructor(s)
    //	---------------------------------------------------------------------------

    /**
     * Constructor needed for Serialable interface
     */
    public ERXDefaultEditingContextDelegate() {}
    
    //	===========================================================================
    //	Instance Variable(s)
    //	---------------------------------------------------------------------------

    /**
     * flag that can tell if the editing context is in
     * the middle of a will save changes call.
     */
    // FIXME inherently single threaded
    private boolean _isInWillSaveChanges=false;    
    
    //	===========================================================================
    //	Instance Method(s)
    //	---------------------------------------------------------------------------
    
    /**
     * Can tell if the delegate is in the middle of a
     * call to will save changes.
     * @return if the delegate is already processing
     * 		a request at the time when another request
     *		comes in.
     */
    public boolean isInWillSaveChanges() { return _isInWillSaveChanges; }
    
    /**
     * Enumerates through all of the objects that have been
     * changed, inserted and deleted calling the appropriate
     * will* method, willInsert, etc. on each of the objects
     * if they are of type ERXGenericRecord. Note that this
     * method is called before validateForSave is called on
     * any of the objects.
     * @param ec editing context that is about to be saved.
     */
    public void editingContextWillSaveChanges(EOEditingContext ec) throws Throwable {
        try {
            if (log.isDebugEnabled()) log.debug("EditingContextWillSaveChanges: start calling will*");            
            _isInWillSaveChanges=true;                      
            if (ec != null && ec.hasChanges()) {
                NSNotificationCenter.defaultCenter().postNotification(ERXExtensions.objectsWillChangeInEditingContext, ec);
                // Changed objects
                if (ec.updatedObjects()!=null && ec.updatedObjects().count()>0) {
                    for (Enumeration e = ec.updatedObjects().objectEnumerator(); e.hasMoreElements();) {
                        EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                        if (eo instanceof ERXGenericRecord)
                            ((ERXGenericRecord)eo).willUpdate();
                    }
                }
                // Deleted objects
                if (ec.deletedObjects()!=null && ec.deletedObjects().count()>0) {
                    for (Enumeration e = ec.deletedObjects().objectEnumerator(); e.hasMoreElements();) {
                        EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                        if (eo instanceof ERXGenericRecord)
                            ((ERXGenericRecord)eo).willDelete();
                    }                    
                }
                // Inserted objects
                if (ec.insertedObjects()!=null && ec.insertedObjects().count()>0) {
                    for (Enumeration e = ec.insertedObjects().objectEnumerator(); e.hasMoreElements();) {
                        EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                        if (eo instanceof ERXGenericRecord && !ec.deletedObjects().containsObject(eo))
                            ((ERXGenericRecord)eo).willInsert();
                    }
                }                
                if (log.isDebugEnabled()) log.debug("EditingContextWillSaveChanges: done calling will*");
                if (logMod.isDebugEnabled()) {
                    if (ec.updatedObjects()!=null) logMod.debug("** Updated Objects "+ec.updatedObjects().count()+" - "+ec.updatedObjects());
                    if (ec.insertedObjects()!=null) logMod.debug("** Inserted Objects "+ec.insertedObjects().count()+" - "+ec.insertedObjects());
                    if (ec.deletedObjects()!=null) logMod.debug("** Deleted Objects "+ec.deletedObjects().count()+" - "+ec.deletedObjects());
                }
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) // i want this stack trace shown in one of the two categories, but not both
                log.debug("Stack Trace:\n" + ERXUtilities.stackTrace(e));
            else if (logMod.isDebugEnabled())
                logMod.debug("Stack Trace:\n" + ERXUtilities.stackTrace(e));
            try {
                StringBuffer buffer = new StringBuffer();
                buffer.append("The following exception has occurred with ec: " + ec + "\n" + ERXUtilities.stackTrace(e) + "\n");
                if (ec.updatedObjects()!=null) {
                    buffer.append("** Updated Objects "+ec.updatedObjects().count());
                    if (logMod.isInfoEnabled()) {
                        for (Enumeration en = ec.updatedObjects().objectEnumerator(); en.hasMoreElements();) {
                            buffer.append('\n');
                            buffer.append(toDebugString((EOEnterpriseObject)en.nextElement()));
                        }
                    }
                }
                if (ec.insertedObjects()!=null) {
                    buffer.append("\n** Inserted Objects "+ec.insertedObjects().count());
                    if (logMod.isInfoEnabled()) {
                        for (Enumeration en = ec.insertedObjects().objectEnumerator(); en.hasMoreElements();) {
                            buffer.append('\n');
                            buffer.append(toDebugString((EOEnterpriseObject)en.nextElement()));
                        }
                    }
                }
                if (ec.deletedObjects()!=null) {
                    buffer.append("\n** Deleted Objects "+ec.deletedObjects().count());
                    if (logMod.isInfoEnabled()) {
                        for (Enumeration en = ec.deletedObjects().objectEnumerator(); en.hasMoreElements();) {
                            buffer.append('\n');
                            buffer.append(toDebugString((EOEnterpriseObject)en.nextElement()));
                        }
                    }
                }
                logMod.error(buffer);
            } catch (Throwable e2) {
                log.error("Caught "+e2+" trying to print editing context state");
            } finally {
                _isInWillSaveChanges=false;                
            }
            throw e;
        } finally {
            _isInWillSaveChanges=false;
        }
    }

    /**
     * When invalidating an object their local
     * cache is flushed by calling the method: <code>
     * flushCaches</code> on the enterprise object if
     * it is an instance of ERXGenericRecord.
     * @param anEditingContext current editing context
     * @param anObject enterprise object to be invlidated
     * @param anEOGlobalID global id to be invalidated
     * @return true
     */
    public boolean editingContextShouldInvalidateObject(EOEditingContext anEOEditingContext,
                                                        EOEnterpriseObject anObject,
                                                        EOGlobalID anEOGlobalID) {
        if (anObject instanceof ERXGenericRecord) {
            ((ERXGenericRecord)anObject).flushCaches();
        }
        return true;
    }

    /**
     * When merging changes into an object their local
     * cache is flushed by calling the method: <code>
     * flushCaches</code> on the enterprise object if
     * it is an instance of ERXGenericRecord.
     * @param anEditingContext current editing context
     * @param object enterprise object to have changes
     *		merged into it.
     * @return true
     */
    public boolean editingContextShouldMergeChangesForObject(EOEditingContext anEditingContext,
                                                             EOEnterpriseObject object) {
        if (object instanceof ERXGenericRecord) {
            ((ERXGenericRecord)object).flushCaches();
        }
        return true;
    }

    public void editingContextWillRevertObjects(EOEditingContext ec, NSArray insertedObjects, NSArray updatedObjects, NSArray deletedObjects) {
        final NSMutableArray objects = new NSMutableArray();

        if ( log.isDebugEnabled() )
            log.debug("editingContextWillRevertObjects: inserted count: " + insertedObjects.count() +
                      ", updated count: " + updatedObjects.count() + ", deleted count: " + deletedObjects.count());

        objects.addObjectsFromArray(insertedObjects);
        objects.addObjectsFromArray(updatedObjects);
        objects.addObjectsFromArray(deletedObjects);

        if ( objects.count() > 0 ) {
            final Enumeration e = objects.objectEnumerator();

            while ( e.hasMoreElements() ) {
                final Object theObject = e.nextElement();

                if ( theObject instanceof ERXGenericRecord )
                    ((ERXGenericRecord)theObject).willRevert();
            }
        }
    }

    /**
     * Called by the editing context following a revert.  This delegate method calls <code>didRevert</code>
     * on any EO that is an instance of ERXGenericRecord.
     * @param ec current editing context.
     * @param insertedObjects objects that were marked as inserted before the revert.
     * @param updatedObjects objects that were marked as updated before the revert.
     * @param deletedObjects objects that were marked as deleted before the revert.
     */
    public void editingContextDidRevertObjects(EOEditingContext ec, NSArray insertedObjects, NSArray updatedObjects, NSArray deletedObjects) {
        final NSMutableArray objects = new NSMutableArray();

        if ( log.isDebugEnabled() )
            log.debug("editingContextDidRevertObjects: inserted count: " + insertedObjects.count() +
                      ", updated count: " + updatedObjects.count() + ", deleted count: " + deletedObjects.count());

        objects.addObjectsFromArray(insertedObjects);
        objects.addObjectsFromArray(updatedObjects);
        objects.addObjectsFromArray(deletedObjects);

        if ( objects.count() > 0 ) {
            final Enumeration e = objects.objectEnumerator();

            while ( e.hasMoreElements() ) {
                final Object theObject = e.nextElement();

                if ( theObject instanceof ERXGenericRecord )
                    ((ERXGenericRecord)theObject).didRevert(ec);
            }
        }
    }

}
