/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERSharedEOLoader.java created by max on Wed 07-Mar-2001 */
package er.extensions;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

// Note: This is a direct port of Kelly Hawks' ObjC SharedEOLoader.  Only enhanced it to use the log4j system.
//
// Kelly's description of the problem:
//
// Now on to the problem. Without source code and symbols, I can't say exactly
// where things go wrong, but the bug occurs when:
// 1) you have an application with more than one model in a model group
// 2) there are cross-model relationships
// 3) The first object you fetch from the database is from a model that doesn't
// contain shared objects. What seems to be happening is EOF's shared EO loader goes like this:
// 1) request comes in for object from model #1
// 2) shared EOs are loaded for model #1 (none).
// 3) object from step #1 touches are relationship to an entity in model #2.
// 4) shared EOs are loaded for model #2.
// For some reason, this is too late. All the shared EOs for all models need to
// be loaded at once.
// The bug manifests itself as:
// Aug 13 00:07:22 FooApplication [24239] *** Uncaught exception:
// <NSInternalInconsistencyException> sqlStringForKeyValueQualifier:: attempt
// to generate SQL for EOKeyValueQualifier 0x441298
// '(someRelationship.someAttribute = 'someValue')' failed because attribute
// identified by key 'someRelationship.someAttribute' was not reachable from
// from entity 'someEntity'

// CHECKME: I believe this bug has been fixed, should be removed if this is the case.
/**
 * Java port of Kelly Hawk's shared EO loader. Works around a bug with shared eos and multiple models.<br />
 * 
 */

public class ERXSharedEOLoader {
    /////////////////////////////////////////  log4j category  ///////////////////////////////////////
    public static final Logger log = Logger.getLogger("er.extensions.fixes.ERSharedEOLoader");

    /** holds the key to enable patched shared eo loading */
    public static final String PatchSharedEOLoadingPropertyKey = "er.extensions.ERXSharedEOLoader.PatchSharedEOLoading";
    
    public static ERXSharedEOLoader _defaultLoader;
    protected static boolean _loadingComplete = false;

    // enables the patch by creating a ERSharedEOLoader object and disabling
    // EOF's sharedObjectLoading via a call to EODatabaseContext.
    public static void patchSharedEOLoading() {
        if (_defaultLoader == null) {
            EODatabaseContext.setSharedObjectLoadingEnabled(false);
            _defaultLoader = new ERXSharedEOLoader();
            ERXRetainer.retain(_defaultLoader); // Needs to be retained on the objC side to recieve notifications.
            log.debug("Shared EO loading patch installed.");
        }
    }

    // This disables the patch, but leaves shared object loading off.
    // To completely restore default EOF behavior, call
    // [EODatabaseContext setSharedObjectLoadingEnabled:YES];
    public static void removeSharedEOLoadingPatch() {
        if (_defaultLoader != null) {
            ERXRetainer.release(_defaultLoader);
            _defaultLoader = null;
        }
        if (_loadingComplete) {
            log.debug("the patch has been removed, but after shared EO loading completed; this call had no effect");
        } else {
            log.debug("shared EO loading patch UNINSTALLED.");
        }
    }

    protected NSMutableArray _modelList = new NSMutableArray();
    protected int _transCount = 0;
    protected boolean _didChangeDebugSetting = false;
    protected EOAdaptorContext _currentAdaptor;
    
    // Sets the receiver up as an observer for the EOModelAddedNotification
    // and the EOCooperatingObjectStoreWasAdded notification.
    public ERXSharedEOLoader() {
        NSNotificationCenter.defaultCenter().addObserver(this,
                                                         new NSSelector("modelWasAddedNotification", ERXConstant.NotificationClassArray),
                                                         EOModelGroup.ModelAddedNotification,
                                                         null);
        NSNotificationCenter.defaultCenter().addObserver(this,
                                                         new NSSelector("objectStoreWasAdded", ERXConstant.NotificationClassArray),
                                                         EOObjectStoreCoordinator.CooperatingObjectStoreWasAddedNotification,
                                                         null);
    }

    public void finalize() throws Throwable {
        NSNotificationCenter.defaultCenter().removeObserver(this);
        super.finalize();
    }

    // actually carries out the loading of shared EOs.
    public void loadSharedObjectsForModel(EOModel aModel) {
        NSArray entities = aModel.entitiesWithSharedObjects();
        if (entities != null && entities.count() > 0) {
        // calling defaultSharedEditingContext "turns on" sharing, so don't
        // call it unless we know there are objects to preload.
        EOSharedEditingContext dsec = EOSharedEditingContext.defaultSharedEditingContext();

            if (dsec != null) {
            // Load the shared EOs
            for (Enumeration e = entities.objectEnumerator(); e.hasMoreElements();) {
                EOEntity entity = (EOEntity)e.nextElement();
                /* For EOs that are completely shared the below for loop results in *2* fetches
                for 1 fs (fetchAll) when the entity is caching the code below works around this with only 1 fetch. */
                if (entity.sharedObjectFetchSpecificationNames().count() == 1 &&
                    entity.sharedObjectFetchSpecificationNames().lastObject().equals("FetchAll")) {
                    try {
                        EOFetchSpecification fs = entity.fetchSpecificationNamed("FetchAll"); 
                        dsec.bindObjectsWithFetchSpecification(fs, "FetchAll"); 
                    } catch (Exception e1) {
                        log.error("Exception occurred for entity named: " + entity.name() + " in Model: " + aModel.name() + e1);
                        throw new RuntimeException(e.toString());
                    }
                } else {
                    for (Enumeration ee = entity.sharedObjectFetchSpecificationNames().objectEnumerator(); ee.hasMoreElements();) {
                        String fsn = (String)ee.nextElement();
                        EOFetchSpecification fs = entity.fetchSpecificationNamed(fsn);
                        if (fs != null) {
                            log.debug("Loading "+entity.name()+" - "+fsn);
                            dsec.bindObjectsWithFetchSpecification(fs, fsn);
                        }
                    }                    
                }
            }
        }
    }
    }
    
    // As models are added to the application, this method receives
    // notifications about them and stores them off to an array.
    public void modelWasAddedNotification(NSNotification aNotification) {
        // sometimes a model gets added twice; make sure we store it once.
        if (!_modelList.containsObject(aNotification.object())) {
            log.debug("Adding model: " + ((EOModel)aNotification.object()).name());
            _modelList.addObject(aNotification.object());
        }
    }

    // The first time an object store is added to EOF, this class iterates
    // over all the models that heard about via EOModelAddedNotification's
    // and loads all their shared EOs at once.
    public void objectStoreWasAdded(NSNotification aNotification) {
        if (!_loadingComplete) {
            if (_modelList.count() == 0) {
                EOModelGroup group = EOModelGroup.modelGroupForObjectStoreCoordinator((EOObjectStoreCoordinator)aNotification.object());
                if (group != null && group.models().count() == 0) {
                    throw new RuntimeException("No models found in default group");
                }
                // internal list empty; drop it and use modelgroup's.
                _modelList = new NSMutableArray(group.models());
            }
            _loadingComplete = true; // make sure we only do this once.
            EOModel currentModel = null;
            try {
                NSNotificationCenter.defaultCenter().addObserver(this,
                                                                 new NSSelector("transactionBeginning", ERXConstant.NotificationClassArray),
                                                                 EOAdaptorContext.AdaptorContextBeginTransactionNotification,
                                                                 null);
                log.debug("Beginning loading of shared EOs");
                NSMutableArray loadedModels = new NSMutableArray();
                for (Enumeration e = _modelList.objectEnumerator(); e.hasMoreElements();) {
                    currentModel = (EOModel)e.nextElement();
                    if (!loadedModels.containsObject(currentModel.name())) {
                        loadSharedObjectsForModel(currentModel);
                        loadedModels.addObject(currentModel.name());
                    }
                }
                NSNotificationCenter.defaultCenter().removeObserver(this, EOAdaptorContext.AdaptorContextBeginTransactionNotification, null);
                if (_didChangeDebugSetting) {
                    //_currentAdaptor.setDebugEnabled(true);
                    _didChangeDebugSetting = false;
                }
                if (_transCount != 0) {
                    // only print this if we loaded something; otherwise
                    // the request for the reg. obj. count with start sharing.
                    log.debug("Shared EO loading complete: " + _transCount + " transactions/ " +
                              EOSharedEditingContext.defaultSharedEditingContext().registeredObjects().count() + " objects.");
                } else {
                    log.debug("Shared EO loading complete: no objects loaded.");
                }
            } catch (Exception e) {
                log.error("Exception occurred with model: " + currentModel.name() + "\n" + e + ERXUtilities.stackTrace());
                // no matter what happens, un-register for notifications.
                NSNotificationCenter.defaultCenter().removeObserver(this, EOAdaptorContext.AdaptorContextBeginTransactionNotification, null);
                if (_didChangeDebugSetting) {
                    //_currentAdaptor.setDebugEnabled(true);
                    _didChangeDebugSetting = false;
                }
            }
        }
    }

    // The method catches EOAdaptorContextBeginTransactionNotification
    // notifications while the loading of shared objects is happening and
    // shuts off debugging messages if the adaptor has debugging enabled.
    public void transactionBeginning(NSNotification aNotification) {
        _currentAdaptor = (EOAdaptorContext)aNotification.object();
        /*
        if (_currentAdaptor.isDebugEnabled() && !ERXExtensions.sharedEOAdaptorCategory.isDebugEnabled()) {
            _didChangeDebugSetting = true;
            _currentAdaptor.setDebugEnabled(false);
            if (log.isDebugEnabled())
                log.debug("Disabling adaptor debugging while loading shared EOs...");
        } */
        _transCount++;
    }
}
