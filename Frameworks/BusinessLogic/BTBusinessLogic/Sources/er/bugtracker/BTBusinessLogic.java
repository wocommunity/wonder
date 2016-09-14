/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.bugtracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;

import er.attachment.ERAttachmentPrincipal;
import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXMainRunner;
import er.extensions.eof.ERXEC;
import er.prototypes.ERPrototypes;
import er.taggable.ERTaggableEntity;

public class BTBusinessLogic extends ERXFrameworkPrincipal {

    public final static Class REQUIRES[] = new Class[] {ERXExtensions.class, ERPrototypes.class, ERCoreBusinessLogic.class, ERAttachmentPrincipal.class};
     
    static {
        setUpFrameworkPrincipalClass(BTBusinessLogic.class);
    }

    static BTBusinessLogic sharedInstance;
    public static BTBusinessLogic sharedInstance() {
        if(sharedInstance == null) {
            sharedInstance = ERXFrameworkPrincipal.sharedInstance(BTBusinessLogic.class);
        }
        return sharedInstance;
    }

    @Override
    public void finishInitialization() {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            EOModel model = EOModelGroup.defaultGroup().modelNamed("BugTracker");
            EOEntity release = model.entityNamed("Release");
            if(model.connectionDictionary().toString().toLowerCase().indexOf(":mysql") >= 0) {
                release.setExternalName("`RELEASE`");
            } else if(model.connectionDictionary().toString().toLowerCase().indexOf(":derby") >= 0) {
                // AK: if we set the connection string to ;create=true, then subsequent model create scripts will 
                // delete former entries, so we set this once here.
                String url = ""+ model.connectionDictionary().objectForKey("URL");
                if(!url.contains(";create=true")) {
                    try {
                        Class foundDriver = Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                        try (Connection conn = DriverManager.getConnection(url +";create=true")) {
                            Statement s = conn.createStatement();
                        }
                    } catch (SQLException e) {
                        //ignore
                    } catch (ClassNotFoundException e) {
                        throw NSForwardException._runtimeExceptionForThrowable(e);
                    }
                }
            }

            ERCoreBusinessLogic.sharedInstance().addPreferenceRelationshipToActorEntity(People.ENTITY_NAME, "id");
            ERTaggableEntity.registerTaggable(Bug.ENTITY_NAME);
        } catch(JDBCAdaptorException ex) {
            if(!(ERXApplication.erxApplication() instanceof ERXMainRunner)) {
                throw ex;
            }
        } finally {
            ec.unlock();
        }
    }

    private static boolean _sharedDataInitialized = false;
    
    // Shared Data Init Point.  Keep alphabetical
    public static void initializeSharedData() {
    	if (!_sharedDataInitialized) {
    	  _sharedDataInitialized = true;
          State.clazz.initializeSharedData();
          Priority.clazz.initializeSharedData();
          TestItemState.clazz.initializeSharedData();
          ERTaggableEntity.registerTaggable(Bug.ENTITY_NAME);
    	}
     }
    
    @Override
    public void didFinishInitialization() {
        BTBusinessLogic.initializeSharedData();
    }
}
