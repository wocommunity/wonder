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
import org.apache.log4j.Category;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// This delegate handles two situations a) allowing EOs to generate their own primary keys and b) working around a
// 	problem with the Oracle adaptor handling dropped database connections.
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class ERXDatabaseContextDelegate {
    ///////////////////////////////////////////////  log4j category  //////////////////////////////////////////////
    public final static Category cat = Category.getInstance(ERXDatabaseContextDelegate.class);
    public final static Category dbCat = Category.getInstance("er.transaction.adaptor.EOAdaptorDebugEnabled.BackTrace");

    private static ERXDatabaseContextDelegate _defaultDelegate;
    public static ERXDatabaseContextDelegate defaultDelegate() {
        if (_defaultDelegate == null) {
            _defaultDelegate = new ERXDatabaseContextDelegate();
            cat.info("created default delegate");
            ERXRetainer.retain(_defaultDelegate); // Retaining the delegate on the ObjC side.  This might not be necessary.
        }
        return _defaultDelegate;
    }
    
    public NSDictionary databaseContextNewPrimaryKey(EODatabaseContext aDatabaseContext, Object object, EOEntity anEntity) {
        return object instanceof ERXGeneratesPrimaryKeyInterface ? ((ERXGeneratesPrimaryKeyInterface)object).primaryKeyDictionary(true) : null;
    }
    
    // This is needed because the OracleEOAdaptor doesn't correctly handle all exceptions of dropped connections.
    public boolean databaseContextShouldHandleDatabaseException(EODatabaseContext dbc, Exception e) throws Throwable {
        EOAdaptor adaptor=dbc.adaptorContext().adaptor();
        boolean shouldHandleConnection = false;
        if(e instanceof EOGeneralAdaptorException)
            cat.info(((EOGeneralAdaptorException)e).userInfo());
        else
            cat.info(e);
        if (adaptor.isDroppedConnectionException(e))
            shouldHandleConnection = true;
        else if (e.toString().indexOf("ORA-01041")!=-1) {
            // just returning true here does not seem to do the trick. why !?!?
            cat.error("ORA-01041 detecting -- forcing reconnect");
            dbc.database().handleDroppedConnection();
            shouldHandleConnection = false;
        } else {
            if(e instanceof EOGeneralAdaptorException)
                cat.info(((EOGeneralAdaptorException)e).userInfo());
            throw e;
        }
        return shouldHandleConnection;
    }

    // This is Kelly Hawks' fix for the missing to one relationship. 
    // Delegate on EODatabaseContext that gets called when a to-one fault cannot find its data in
    // the database. The object is a cleared fault. We raise here to restore the functionality
    // that existed prior to WebObjects 4.5.
    // Whenever a fault fails for a globalID (i.e. the object is NOT found in the database), we may raise.
    public boolean databaseContextFailedToFetchObject(EODatabaseContext context, Object object, EOGlobalID gid) {
        if (object!=null) {
            EOEditingContext ec = ((EOEnterpriseObject)object).editingContext();

            // we need to refault the object before raising, otherwise, if the caller traps
            // the exception, it will be a successful lookup the next time a fault with the
            // same global id fires.  NOTE: refaulting in a sharedEditingContext is illegal,
            // so we specifically check for that special case.

            if (!(ec instanceof EOSharedEditingContext)) {
                context.refaultObject((EOEnterpriseObject)object, gid, ec);
            }
        }
        throw new RuntimeException("NSObjectNotAvailableException No " + (object!=null ? object.getClass().getName() : "N/A") + " found with globalID: " + gid);            
    }
    
    /* Nice debugging adds to track down faulting issues. */
    /*
    public boolean databaseContextShouldFetchObjectFault (EODatabaseContext context, Object object) {
        cat.debug("databaseContextShouldFetchObjectFault, object: " + object.getClass().getName());
        return true;
    }
    public boolean databaseContextShouldFetchArrayFault (EODatabaseContext context, Object object) {
        cat.debug("databaseContextShouldFetchArrayFault, array: " + object.getClass().getName());
        return true;
    }
    public void databaseContextWillFireObjectFaultForGlobalID (EODatabaseContext context, EOGlobalID globalID, EOFetchSpecification fetchSpec,
                                                               EOEditingContext ec) {
        cat.debug("databaseContextWillFireObjectFaultForGlobalID");//, fetchSpec: " + fetchSpec);
    }
     
    public void databaseContextWillFireArrayFaultForGlobalID (EODatabaseContext context, EOGlobalID globalID, EORelationship relationship,
                                                              EOFetchSpecification fetchSpec, EOEditingContext ec) {
        //cat.debug("databaseContextWillFireArrayFaultForGlobalID, fetchSpec: " + fetchSpec + " relationship: " + relationship);
        cat.debug("databaseContextWillFireArrayFaultForGlobalID, entity: " + entityName() + " relationship: " + relationship);
    }
     */


    public void databaseContextDidSelectObjects(EODatabaseContext dc,
                                                EOFetchSpecification fs,
                                                EODatabaseChannel channel) {
        if (dbCat.isDebugEnabled()) {
            dbCat.debug("databaseContextDidSelectObjects "+fs);
        }
    }

    
}
