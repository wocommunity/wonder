/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;
import java.io.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////////////////////////////
// This guy is pretty much only used in developement systems.  It provides a nice repository about
// files and their last modified dates.  So instead of every dynamic spot having to keep track of
// the files' dates, register and check at the end of every request-response loop, instead you
// can just add an observer to this center and be notified when the file changes.
//////////////////////////////////////////////////////////////////////////////////////////////////
public class ERXFileNotificationCenter {

    ///////////////////////////////////////  log4j category  /////////////////////////////////////
    public static final Category cat = Category.getInstance(ERXFileNotificationCenter.class);

    /////////////////////////////////////// Notification Titles //////////////////////////////////
    public static final String FileDidChange = "FileDidChange";

    private static ERXFileNotificationCenter _defaultCenter;
    public static ERXFileNotificationCenter defaultCenter() {
        if (_defaultCenter == null)
        _defaultCenter = new ERXFileNotificationCenter();
        return _defaultCenter;
    }

    private NSMutableDictionary _observersByFilePath = new NSMutableDictionary();
    private NSMutableDictionary _lastModifiedByFilePath = new NSMutableDictionary();
    // Here we assume that when caching is enabled that we don't want to be checking any files or notifing
    // anyone of changes.
    private boolean cachingEnabled = true;
    public ERXFileNotificationCenter() {
        if (!WOApplication.application().isCachingEnabled()) {
            ERXRetainer.retain(this);
            cat.debug("Caching disabled.  Registering for notification: " + ERXApplication.WORequestHandlerDidHandleRequestNotification);
            NSNotificationCenter.defaultCenter().addObserver(this,
                                                             new NSSelector("checkIfFilesHaveChanged", ERXConstant.NotificationClassArray),
                                                             ERXApplication.WORequestHandlerDidHandleRequestNotification,
                                                             null);            
            cachingEnabled = false;
        }
    }

    public void finalize() throws Throwable {
        NSNotificationCenter.defaultCenter().removeObserver(this);
        super.finalize();
    }

    public void addObserver(Object observer, NSSelector selector, String filePath) {
        if (filePath == null)
            throw new RuntimeException("Attempting to register observer for null filePath.");
        addObserver(observer, selector, new File(filePath));        
    }

    public void addObserver(Object observer, NSSelector selector, File file) {
        if (file == null || !file.exists())
            throw new RuntimeException("Attempting to register a null file. " + (file != null ? " File path: " + file.getAbsolutePath() : null));
        if (observer == null)
            throw new RuntimeException("Attempting to register null observer for file: " + file);
        if (observer == null)
            throw new RuntimeException("Attempting to register null selector for file: " + file);
        if (cachingEnabled)
            cat.warn("Registering an observer when WOCaching is enabled.  This observer will not ever by default be called: " + file);
        String filePath = file.getAbsolutePath();
        if (cat.isDebugEnabled())
            cat.debug("Registering Observer for file at path: " + filePath);
        // Register last modified date.
        registerLastModifiedDateForFile(file);
        // FIXME: This retains the observer.  This is not ideal.  With 5.0 we can use a ReferenceQueue to maintain weak references.
        NSMutableSet observerSet = (NSMutableSet)_observersByFilePath.objectForKey(filePath);
        if (observerSet == null) {
            observerSet = new NSMutableSet();
            _observersByFilePath.setObjectForKey(observerSet, filePath);
        }
        observerSet.addObject(new _ObserverSelectorHolder(observer, selector));
    }

    public void registerLastModifiedDateForFile(File file) {
        if (file != null || !file.exists())
            _lastModifiedByFilePath.setObjectForKey(new Long(file.lastModified()), file.getAbsolutePath());
    }

    public boolean hasFileChanged(File file) {
        if (file == null)
            throw new RuntimeException("Attempting to check if a null file has been changed");
        Long lastModified = (Long)_lastModifiedByFilePath.objectForKey(file.getAbsolutePath());
        return lastModified == null || file.lastModified() > lastModified.longValue();
    }

    protected void fileHasChanged(File file) {
        NSMutableSet observers = (NSMutableSet)_observersByFilePath.objectForKey(file.getAbsolutePath());
        if (observers == null)
            cat.warn("Unable to find observers for file: " + file);
        else {
            NSNotification notification = new NSNotification(FileDidChange, file);
            for (Enumeration e = observers.objectEnumerator(); e.hasMoreElements();) {
                _ObserverSelectorHolder holder = (_ObserverSelectorHolder)e.nextElement();
                try {
                    holder.selector.invoke(holder.observer, notification);
                } catch (Exception ex) {
                    cat.error("Catching exception when invoking method on observer: " + ex.toString());
                }
            }
            registerLastModifiedDateForFile(file);            
        }
    }
    
    public void checkIfFilesHaveChanged(NSNotification n) {
        if (cat.isDebugEnabled()) cat.debug("Checking if files have changed");
        for (Enumeration e = _lastModifiedByFilePath.keyEnumerator(); e.hasMoreElements();) {
            File file = new File((String)e.nextElement());
            if (file.exists() && hasFileChanged(file)) {
                fileHasChanged(file);
            }
        }
    }
    
    public static class _ObserverSelectorHolder {
        public Object observer;
        public NSSelector selector;
        public _ObserverSelectorHolder(Object observer, NSSelector selector) {
            observer = observer;
            selector = selector;
        }

        public boolean equals(Object osh) {
            return osh != null && osh instanceof _ObserverSelectorHolder && ((_ObserverSelectorHolder)osh).selector.equals(selector) &&
            ((_ObserverSelectorHolder)osh).observer.equals(observer);
        }
    }
}
