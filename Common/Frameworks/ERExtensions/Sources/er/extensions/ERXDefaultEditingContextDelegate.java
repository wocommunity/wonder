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
import org.apache.log4j.Category;

public class ERXDefaultEditingContextDelegate extends ERXEditingContextDelegate {

    ///////////////////////////////////////////  log4j category  /////////////////////////////////////////////////
    public static final Category cat = Category.getInstance(ERXDefaultEditingContextDelegate.class);
    public static final Category catMod = Category.getInstance("er.transaction.delegate.EREditingContextDelegate.modifedObjects");
    
    // FIXME inherently single threaded
    private boolean _isInWillSaveChanges=false;
    public boolean isInWillSaveChanges() { return _isInWillSaveChanges; }
    
    // Enumerates through all changes/inserts/updates calling will* if they are of instance tyep ERXGenericRecords.
    public void editingContextWillSaveChanges(EOEditingContext ec) throws Throwable {
        try {
            if (cat.isDebugEnabled()) cat.debug("EditingContextWillSaveChanges: start calling will*");            
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
                if (cat.isDebugEnabled()) cat.debug("EditingContextWillSaveChanges: done calling will*");
                if (catMod.isDebugEnabled()) {
                    if (ec.updatedObjects()!=null) catMod.debug("** Updated Objects "+ec.updatedObjects().count()+" - "+ec.updatedObjects());
                    if (ec.insertedObjects()!=null) catMod.debug("** Inserted Objects "+ec.insertedObjects().count()+" - "+ec.insertedObjects());
                    if (ec.deletedObjects()!=null) catMod.debug("** Deleted Objects "+ec.deletedObjects().count()+" - "+ec.deletedObjects());
                }
            }
            
        } catch (Throwable e) {
            if (cat.isDebugEnabled()) // i want this stack trace shown in one of the two categories, but not both
                cat.debug("Stack Trace:\n" + ERXUtilities.stackTrace(e));
            else if (catMod.isDebugEnabled())
                catMod.debug("Stack Trace:\n" + ERXUtilities.stackTrace(e));
            try {
                StringBuffer buffer = new StringBuffer();
                buffer.append("The following exception has occurred with ec: " + ec + "\n" + ERXUtilities.stackTrace(e) + "\n");
                if (ec.updatedObjects()!=null) {
                    buffer.append("** Updated Objects "+ec.updatedObjects().count());
                    for (Enumeration en = ec.updatedObjects().objectEnumerator(); en.hasMoreElements();)
                        buffer.append("\n" + toDebugString((EOEnterpriseObject)en.nextElement()));
                }
                if (ec.insertedObjects()!=null) {
                    buffer.append("\n** Inserted Objects "+ec.insertedObjects().count());
                    for (Enumeration en = ec.insertedObjects().objectEnumerator(); en.hasMoreElements();)
                        buffer.append(toDebugString((EOEnterpriseObject)en.nextElement()));
                }
                if (ec.deletedObjects()!=null) {
                    buffer.append("\n** Deleted Objects "+ec.deletedObjects().count());
                    for (Enumeration en = ec.deletedObjects().objectEnumerator(); en.hasMoreElements();)
                        buffer.append(toDebugString((EOEnterpriseObject)en.nextElement()));
                }
                catMod.error(buffer);
            } catch (Throwable e2) {
                cat.error("Caught "+e2+" trying to print editing context state");
            } finally {
                _isInWillSaveChanges=false;                
            }
            throw e;
        } finally {
            _isInWillSaveChanges=false;
        }

    }

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

    public boolean editingContextShouldInvalidateObject(EOEditingContext anEOEditingContext,
                                                        EOEnterpriseObject anObject,
                                                        EOGlobalID anEOGlobalID) {
        if (anObject instanceof ERXGenericRecord) {
            ((ERXGenericRecord)anObject).flushCaches();
        }
        return true;
    }


    public boolean editingContextShouldMergeChangesForObject(EOEditingContext anEditingContext,
                                                             EOEnterpriseObject object) {
        if (object instanceof ERXGenericRecord) {
            ((ERXGenericRecord)object).flushCaches();
        }
        return true;
    }

}
